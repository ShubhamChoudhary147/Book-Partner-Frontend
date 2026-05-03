package com.cg.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountDto {

    // DiscountProjection exposes: discounttype, lowqty, highqty, discount
    // storId (FK) is hidden per backend design

    @JsonProperty("discounttype")
    private String discounttype;

    @JsonProperty("lowqty")
    private Integer lowqty;

    @JsonProperty("highqty")
    private Integer highqty;

    @JsonProperty("discount")
    private BigDecimal discount;

    // storId needed for POST but not displayed
    @JsonProperty("storId")
    private String storId;

    public DiscountDto() {}

    public String getDiscounttype() { return discounttype; }
    public void setDiscounttype(String discounttype) { this.discounttype = discounttype; }

    public Integer getLowqty() { return lowqty; }
    public void setLowqty(Integer lowqty) { this.lowqty = lowqty; }

    public Integer getHighqty() { return highqty; }
    public void setHighqty(Integer highqty) { this.highqty = highqty; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getStorId() { return storId; }
    public void setStorId(String storId) { this.storId = storId; }
}
