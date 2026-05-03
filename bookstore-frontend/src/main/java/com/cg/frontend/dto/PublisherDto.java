package com.cg.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublisherDto {

    private String pubId;
    private String pubName;
    private String city;
    private String state;
    private String country;

    // ── Getters & Setters ─────────────────────────────

    public String getPubId() { return pubId; }
    public void setPubId(String pubId) { this.pubId = pubId; }

    public String getPubName() { return pubName; }
    public void setPubName(String pubName) { this.pubName = pubName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    /** Convenience: full display label */
    public String getDisplayName() {
        return pubName != null ? pubName : pubId;
    }
}
