package com.cg.frontend.dto;

import java.math.BigDecimal;

public class PublisherTitleDto {

    private String titleId;
    private String title;
    private String type;
    private BigDecimal price;
    private Integer ytdSales;
    private String pubId;

    // ── Getters & Setters ─────────────────────────────

    public String getTitleId() { return titleId; }
    public void setTitleId(String titleId) { this.titleId = titleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getYtdSales() { return ytdSales; }
    public void setYtdSales(Integer ytdSales) { this.ytdSales = ytdSales; }

    public String getPubId() { return pubId; }
    public void setPubId(String pubId) { this.pubId = pubId; }
}
