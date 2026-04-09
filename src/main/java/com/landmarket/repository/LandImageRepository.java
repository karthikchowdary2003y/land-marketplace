package com.landmarket.repository;

import com.landmarket.model.LandImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LandImageRepository extends JpaRepository<LandImage, Long> {
    List<LandImage> findByLandIdOrderByDisplayOrderAsc(Long landId);
    void deleteByLandId(Long landId);
    long countByLandId(Long landId);
}