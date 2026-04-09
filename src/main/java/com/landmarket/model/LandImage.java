package com.landmarket.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
@Data
@Entity
@Table(name = "land_images")
public class LandImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "land_id", nullable = false)
    @JsonIgnore
    private Land land;

    @Column(nullable = false)
    private String imagePath;

    @Column(nullable = false)
    private String imageUrl;

    private Integer displayOrder;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}