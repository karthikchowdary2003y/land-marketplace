package com.landmarket.dto;

import com.landmarket.model.Land;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LandSearchRequest {

    private String keyword;
    private String city;
    private String state;
    private Land.LandType landType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minArea;
    private Double maxArea;
    private Land.LandStatus status = Land.LandStatus.AVAILABLE;
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
