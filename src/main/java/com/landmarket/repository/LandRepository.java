package com.landmarket.repository;

import com.landmarket.model.Land;
import com.landmarket.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LandRepository extends JpaRepository<Land, Long> {

    // Get all active lands with pagination
    Page<Land> findByActiveTrue(Pageable pageable);

    // Search by city
    Page<Land> findByCityIgnoreCaseAndActiveTrue(String city, Pageable pageable);

    // Search by state
    Page<Land> findByStateIgnoreCaseAndActiveTrue(String state, Pageable pageable);

    // Filter by land type
    Page<Land> findByLandTypeAndActiveTrue(Land.LandType landType, Pageable pageable);

    // Filter by status
    Page<Land> findByStatusAndActiveTrue(Land.LandStatus status, Pageable pageable);

    // Get lands by owner
    List<Land> findByOwnerAndActiveTrue(User owner);

    // Advanced search
    @Query("SELECT l FROM Land l WHERE l.active = true " +
           "AND (:city IS NULL OR LOWER(l.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:state IS NULL OR LOWER(l.state) LIKE LOWER(CONCAT('%', :state, '%'))) " +
           "AND (:landType IS NULL OR l.landType = :landType) " +
           "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.price <= :maxPrice) " +
           "AND (:minArea IS NULL OR l.areaInAcres >= :minArea) " +
           "AND (:maxArea IS NULL OR l.areaInAcres <= :maxArea) " +
           "AND (:status IS NULL OR l.status = :status)")
    Page<Land> searchLands(
            @Param("city") String city,
            @Param("state") String state,
            @Param("landType") Land.LandType landType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minArea") Double minArea,
            @Param("maxArea") Double maxArea,
            @Param("status") Land.LandStatus status,
            Pageable pageable
    );

    // Full text search on title and description
    @Query("SELECT l FROM Land l WHERE l.active = true AND " +
           "(LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.state) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Land> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Count active listings
    long countByActiveTrue();
}
