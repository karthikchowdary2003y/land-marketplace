package com.landmarket.dto;

import com.landmarket.model.Land;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LandRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private Double latitude;
    private Double longitude;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "0.01", message = "Area must be positive")
    private Double areaInAcres;

    @NotNull(message = "Land type is required")
    private Land.LandType landType;

    private Land.LandStatus status = Land.LandStatus.AVAILABLE;

    private String surveyNumber;
    private String documentNumber;

    private Land.RoadAccess roadAccess;
    private boolean waterSource;
    private boolean electricity;
    private boolean fencing;
}
