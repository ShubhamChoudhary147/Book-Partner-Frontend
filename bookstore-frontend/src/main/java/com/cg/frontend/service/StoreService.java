package com.cg.frontend.service;

import com.cg.frontend.dto.DiscountDto;
import com.cg.frontend.dto.PageMetaDto;
import com.cg.frontend.dto.SaleDto;
import com.cg.frontend.dto.StoreDto;
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
public class StoreService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${backend.base-url}")
    private String baseUrl;

    public StoreService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * StoreProjection hides storId from the response body.
     * But Spring Data REST always includes _links.self with the full URL.
     * Extract the ID from: _links -> self -> href -> last path segment.
     * e.g. "http://localhost:8085/api/stores/6380" -> "6380"
     */
    private String extractStorId(JsonNode node) {
        try {
            String href = node.path("_links").path("self").path("href").asText("");
            if (href.isEmpty()) return null;
            // Remove trailing slash if present, then take last segment
            if (href.endsWith("/")) href = href.substring(0, href.length() - 1);
            return href.substring(href.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private StoreDto nodeToStoreDto(JsonNode node) throws Exception {
        StoreDto s = objectMapper.treeToValue(node, StoreDto.class);
        // storId is null from projection — populate it from the self-link
        if (s.getStorId() == null || s.getStorId().isBlank()) {
            s.setStorId(extractStorId(node));
        }
        return s;
    }

    // ── List (paginated) ──────────────────────────────────────────────────────

    public List<StoreDto> getAllStores(int page, int size) {
        try {
            String url = baseUrl + "/stores?page=" + page + "&size=" + size;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode embedded = response.getBody().path("_embedded").path("stores");
            List<StoreDto> stores = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    stores.add(nodeToStoreDto(node));
                }
            }
            return stores;
        } catch (Exception e) {
            System.err.println("[StoreService] getAllStores failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public PageMetaDto getPageMeta(int page, int size) {
        try {
            String url = baseUrl + "/stores?page=" + page + "&size=" + size;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode pageMeta = response.getBody().path("page");
            return objectMapper.treeToValue(pageMeta, PageMetaDto.class);
        } catch (Exception e) {
            System.err.println("[StoreService] getPageMeta failed: " + e.getMessage());
            return new PageMetaDto();
        }
    }

    // ── Search (exact field match) ─────────────────────────────────────────────

    public List<StoreDto> searchStores(String field, String value) {
        try {
            String url = baseUrl + "/stores?page=0&size=200";
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode embedded = response.getBody().path("_embedded").path("stores");
            List<StoreDto> stores = new ArrayList<>();
            String v = value.trim().toLowerCase();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    StoreDto s = nodeToStoreDto(node);
                    String fieldVal = getField(s, field);
                    if (fieldVal != null && fieldVal.toLowerCase().equals(v)) {
                        stores.add(s);
                    }
                }
            }
            return stores;
        } catch (Exception e) {
            System.err.println("[StoreService] searchStores failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getField(StoreDto s, String field) {
        switch (field) {
            case "storName":    return s.getStorName();
            case "city":        return s.getCity();
            case "state":       return s.getState();
            case "zip":         return s.getZip();
            case "storAddress": return s.getStorAddress();
            default:            return null;
        }
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    public StoreDto getStoreById(String storId) {
        try {
            String url = baseUrl + "/stores/" + storId;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            // Detail endpoint returns full entity (not projection), so storId is present
            return objectMapper.treeToValue(response.getBody(), StoreDto.class);
        } catch (Exception e) {
            System.err.println("[StoreService] getStoreById failed for id=" + storId + ": " + e.getMessage());
            return null;
        }
    }

    // ── Sales for store ───────────────────────────────────────────────────────

    public List<SaleDto> getSalesByStore(String storId) {
        try {
            String url = baseUrl + "/sales/search/findByStorId?storId=" + storId;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode embedded = response.getBody().path("_embedded").path("sales");
            List<SaleDto> sales = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    SaleDto sale = new SaleDto();
                    sale.setOrdNum(node.path("ordNum").asText(null));
                    JsonNode dateNode = node.path("ordDate");
                    if (!dateNode.isMissingNode()) {
                        sale.setOrdDate(formatDate(dateNode));
                    }
                    sale.setQty(node.path("qty").asInt(0));
                    sale.setPayterms(node.path("payterms").asText(null));
                    sales.add(sale);
                }
            }
            return sales;
        } catch (Exception e) {
            System.err.println("[StoreService] getSalesByStore failed for storId=" + storId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String formatDate(JsonNode dateNode) {
        try {
            if (dateNode.isArray() && dateNode.size() >= 3) {
                int year  = dateNode.get(0).asInt();
                int month = dateNode.get(1).asInt();
                int day   = dateNode.get(2).asInt();
                return String.format("%04d-%02d-%02d", year, month, day);
            }
            return dateNode.asText();
        } catch (Exception e) {
            return dateNode.asText();
        }
    }

    // ── Discounts for store ───────────────────────────────────────────────────

    public List<DiscountDto> getDiscountsByStore(String storId) {
        try {
            String url = baseUrl + "/discounts/search/findByStorId?storId=" + storId;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode embedded = response.getBody().path("_embedded").path("discounts");
            List<DiscountDto> discounts = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    DiscountDto d = new DiscountDto();
                    d.setDiscounttype(node.path("discounttype").asText(null));
                    d.setLowqty(node.path("lowqty").isNull() ? null : node.path("lowqty").asInt());
                    d.setHighqty(node.path("highqty").isNull() ? null : node.path("highqty").asInt());
                    if (!node.path("discount").isMissingNode()) {
                        d.setDiscount(new java.math.BigDecimal(node.path("discount").asText("0")));
                    }
                    discounts.add(d);
                }
            }
            return discounts;
        } catch (Exception e) {
            System.err.println("[StoreService] getDiscountsByStore failed for storId=" + storId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public String createStore(StoreDto store) {
        try {
            String url = baseUrl + "/stores";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<StoreDto> request = new HttpEntity<>(store, headers);
            System.out.println("[StoreService] POST " + url + " -> " + objectMapper.writeValueAsString(store));
            ResponseEntity<StoreDto> response = restTemplate.postForEntity(url, request, StoreDto.class);
            System.out.println("[StoreService] createStore response status: " + response.getStatusCode());
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("[StoreService] createStore HTTP error: " + e.getStatusCode() + " body: " + e.getResponseBodyAsString());
            return "Backend error " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("[StoreService] createStore exception: " + e.getMessage());
            return "Connection error: " + e.getMessage();
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public String updateStore(String storId, StoreDto store) {
        try {
            String url = baseUrl + "/stores/" + storId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<StoreDto> request = new HttpEntity<>(store, headers);
            System.out.println("[StoreService] PUT " + url + " -> " + objectMapper.writeValueAsString(store));
            ResponseEntity<StoreDto> response = restTemplate.exchange(url, HttpMethod.PUT, request, StoreDto.class);
            System.out.println("[StoreService] updateStore response status: " + response.getStatusCode());
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("[StoreService] updateStore HTTP error: " + e.getStatusCode() + " body: " + e.getResponseBodyAsString());
            return "Backend error " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("[StoreService] updateStore exception: " + e.getMessage());
            return "Connection error: " + e.getMessage();
        }
    }
}