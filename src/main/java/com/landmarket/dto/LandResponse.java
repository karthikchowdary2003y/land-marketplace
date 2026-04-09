package com.landmarket.dto;

import com.landmarket.model.Land;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LandResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private Double areaInAcres;
    private Land.LandType landType;
    private Land.LandStatus status;
    private String surveyNumber;
    private boolean documentVerified;
    private Land.RoadAccess roadAccess;
    private boolean waterSource;
    private boolean electricity;
    private boolean fencing;
    private List<String> images;
    private int viewCount;
    private LocalDateTime createdAt;

    // *** NO MIDDLEMAN - Direct Owner Contact Info ***
    private OwnerInfo owner;

    @Data
    public static class OwnerInfo {
        private Long id;
        private String fullName;
        private String phone;       // Buyer calls directly!
        private String email;       // Buyer emails directly!
        private String city;
        private String state;
        private String profileImage;
    }
}
