package com.cg.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleDto {

    // SalesProjection exposes: ordNum, ordDate, qty, payterms
    // storId and titleId are internal FK keys — not shown per backend design

    @JsonProperty("ordNum")
    private String ordNum;

    @JsonProperty("ordDate")
    private String ordDate;   // kept as String for safe Thymeleaf display

    @JsonProperty("qty")
    private Integer qty;

    @JsonProperty("payterms")
    private String payterms;

    // These are needed for creating a sale record (POST body) but not shown in list
    @JsonProperty("storId")
    private String storId;

    @JsonProperty("titleId")
    private String titleId;

    public SaleDto() {}

    public String getOrdNum() { return ordNum; }
    public void setOrdNum(String ordNum) { this.ordNum = ordNum; }

    public String getOrdDate() { return ordDate; }
    public void setOrdDate(String ordDate) { this.ordDate = ordDate; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public String getPayterms() { return payterms; }
    public void setPayterms(String payterms) { this.payterms = payterms; }

    public String getStorId() { return storId; }
    public void setStorId(String storId) { this.storId = storId; }

    public String getTitleId() { return titleId; }
    public void setTitleId(String titleId) { this.titleId = titleId; }
}
