package com.cg.frontend.service;

import com.cg.frontend.dto.PublisherDto;
import com.cg.frontend.dto.PublisherTitleDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class PublisherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${backend.base-url}")
    private String baseUrl;

    public PublisherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // ── List (paginated) ──────────────────────────────────────────────────────

    public List<PublisherDto> getAllPublishers(int page, int size) {
        try {
            String url = baseUrl + "/publishers?page=" + page + "&size=" + size;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode embedded = response.getBody().path("_embedded").path("publishers");
            List<PublisherDto> publishers = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    publishers.add(objectMapper.treeToValue(node, PublisherDto.class));
                }
            }
            return publishers;
        } catch (Exception e) {
            System.err.println("[PublisherService] getAllPublishers failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public com.cg.frontend.dto.PageMetaDto getPageMeta(int page, int size) {
        try {
            String url = baseUrl + "/publishers?page=" + page + "&size=" + size;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode pageMeta = response.getBody().path("page");
            return objectMapper.treeToValue(pageMeta, com.cg.frontend.dto.PageMetaDto.class);
        } catch (Exception e) {
            System.err.println("[PublisherService] getPageMeta failed: " + e.getMessage());
            return new com.cg.frontend.dto.PageMetaDto();
        }
    }

    // ── Search (client-side filter) ───────────────────────────────────────────

    public List<PublisherDto> searchPublishers(String query) {
        try {
            String url = baseUrl + "/publishers?page=0&size=100";
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode embedded = response.getBody().path("_embedded").path("publishers");
            List<PublisherDto> publishers = new ArrayList<>();
            String q = query.trim().toLowerCase();  // ← trim add kiya
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    PublisherDto p = objectMapper.treeToValue(node, PublisherDto.class);
                    if (matchesQuery(p, q)) publishers.add(p);
                }
            }
            return publishers;
        } catch (Exception e) {
            System.err.println("[PublisherService] searchPublishers failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean matchesQuery(PublisherDto p, String q) {
        // ← har field pe trim() add kiya — backend CHAR type mein trailing spaces hoti hain
        return (p.getPubId()   != null && p.getPubId().trim().toLowerCase().contains(q))
            || (p.getPubName() != null && p.getPubName().trim().toLowerCase().contains(q))
            || (p.getCity()    != null && p.getCity().trim().toLowerCase().contains(q))
            || (p.getState()   != null && p.getState().trim().toLowerCase().contains(q))
            || (p.getCountry() != null && p.getCountry().trim().toLowerCase().contains(q));
    }

    // ── Single ────────────────────────────────────────────────────────────────

    public PublisherDto getPublisherById(String pubId) {
        try {
            String url = baseUrl + "/publishers/" + pubId;
            ResponseEntity<PublisherDto> response = restTemplate.getForEntity(url, PublisherDto.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("[PublisherService] getPublisherById failed for id=" + pubId + ": " + e.getMessage());
            return null;
        }
    }

    // ── Titles by publisher ───────────────────────────────────────────────────

    public List<PublisherTitleDto> getTitlesByPublisher(String pubId) {
        try {
            String url = baseUrl + "/titles/search/findByPublisherPubId?pubId=" + pubId;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode embedded = response.getBody().path("_embedded").path("titles");
            List<PublisherTitleDto> titles = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    PublisherTitleDto t = new PublisherTitleDto();
                    t.setTitleId(node.path("titleId").asText(null));
                    t.setTitle(node.path("title").asText(null));
                    t.setType(node.path("type").asText(null));
                    JsonNode priceNode = node.path("price");
                    if (!priceNode.isMissingNode() && !priceNode.isNull()) {
                        t.setPrice(new java.math.BigDecimal(priceNode.asText()));
                    }
                    t.setYtdSales(node.path("ytdSales").asInt(0));
                    titles.add(t);
                }
            }
            return titles;
        } catch (Exception e) {
            System.err.println("[PublisherService] getTitlesByPublisher failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public String createPublisher(PublisherDto publisher) {
        try {
            String url = baseUrl + "/publishers";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PublisherDto> request = new HttpEntity<>(publisher, headers);
            System.out.println("[PublisherService] POST " + url + " -> " + objectMapper.writeValueAsString(publisher));
            ResponseEntity<PublisherDto> response = restTemplate.postForEntity(url, request, PublisherDto.class);
            System.out.println("[PublisherService] createPublisher response status: " + response.getStatusCode());
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("[PublisherService] createPublisher HTTP error: " + e.getStatusCode() + " body: " + e.getResponseBodyAsString());
            return "Backend error " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("[PublisherService] createPublisher exception: " + e.getMessage());
            return "Connection error: " + e.getMessage();
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public String updatePublisher(String pubId, PublisherDto publisher) {
        try {
            String url = baseUrl + "/publishers/" + pubId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PublisherDto> request = new HttpEntity<>(publisher, headers);
            System.out.println("[PublisherService] PUT " + url + " -> " + objectMapper.writeValueAsString(publisher));
            ResponseEntity<PublisherDto> response = restTemplate.exchange(url, HttpMethod.PUT, request, PublisherDto.class);
            System.out.println("[PublisherService] updatePublisher response status: " + response.getStatusCode());
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("[PublisherService] updatePublisher HTTP error: " + e.getStatusCode() + " body: " + e.getResponseBodyAsString());
            return "Backend error " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("[PublisherService] updatePublisher exception: " + e.getMessage());
            return "Connection error: " + e.getMessage();
        }
    }
}