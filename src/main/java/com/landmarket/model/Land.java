package com.landmarket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

<<<<<<< HEAD
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

=======
import com.fasterxml.jackson.annotation.JsonIgnore;

>>>>>>> 9669594225bc728bb0f17da2da172c67b996a61e
@Entity
@Table(name = "lands")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Land {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // Location Details
    @NotBlank
    @Column(nullable = false)
    private String address;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    private Double latitude;
    private Double longitude;

    // Land Details
    @NotNull
    @Column(nullable = false)
    private Double areaInAcres;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LandType landType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LandStatus status;

    // Legal Details
    private String surveyNumber;
    private String documentNumber;

    @Column(nullable = false)
    @Builder.Default
    private boolean documentVerified = false;

    // Road Access
    @Enumerated(EnumType.STRING)
    private RoadAccess roadAccess;

    // Water Source
    private boolean waterSource;
    private boolean electricity;
    private boolean fencing;

    // Images stored as comma-separated paths
    @Column(columnDefinition = "TEXT")
    private String images;
    
   

    // Owner Info
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

<<<<<<< HEAD
    @JsonIgnore 
=======
      @JsonIgnore 

>>>>>>> 9669594225bc728bb0f17da2da172c67b996a61e
    @OneToMany(mappedBy = "land", cascade = CascadeType.ALL)
    private List<Inquiry> inquiries;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum LandType {
        AGRICULTURAL, RESIDENTIAL, COMMERCIAL, INDUSTRIAL, FOREST, PLANTATION, OTHER
    }

    public enum LandStatus {
        AVAILABLE, SOLD, UNDER_NEGOTIATION
    }

    public enum RoadAccess {
        NATIONAL_HIGHWAY, STATE_HIGHWAY, VILLAGE_ROAD, PRIVATE_ROAD, NO_ROAD
    }
}
