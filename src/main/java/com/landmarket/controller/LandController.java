package com.landmarket.controller;

import com.landmarket.dto.ApiResponse;
import com.landmarket.dto.LandRequest;
import com.landmarket.dto.LandResponse;
import com.landmarket.dto.LandSearchRequest;
import com.landmarket.model.Land;
import com.landmarket.service.LandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lands")
@CrossOrigin(origins = "*")
public class LandController {

    @Autowired
    private LandService landService;

    /**
     * GET /api/lands
     * Get all available land listings (PUBLIC)
     * Buyer can see all listings with owner contact info
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<LandResponse>>> getAllLands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        Page<LandResponse> lands = landService.getAllLands(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Lands fetched successfully", lands));
    }

    /**
     * GET /api/lands/{id}
     * Get single land with full owner contact (PUBLIC - NO MIDDLEMAN)
     * This is where buyer sees seller phone and can call directly!
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LandResponse>> getLandById(@PathVariable Long id) {
        try {
            LandResponse land = landService.getLandById(id);
            return ResponseEntity.ok(ApiResponse.success("Land fetched successfully", land));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/lands/search
     * Search and filter lands (PUBLIC)
     * Filters: city, state, type, price range, area range
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<LandResponse>>> searchLands(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Land.LandType landType,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        LandSearchRequest searchReq = new LandSearchRequest();
        searchReq.setKeyword(keyword);
        searchReq.setCity(city);
        searchReq.setState(state);
        searchReq.setLandType(landType);
        searchReq.setMinPrice(minPrice);
        searchReq.setMaxPrice(maxPrice);
        searchReq.setMinArea(minArea);
        searchReq.setMaxArea(maxArea);
        searchReq.setPage(page);
        searchReq.setSize(size);
        searchReq.setSortBy(sortBy);
        searchReq.setSortDir(sortDir);

        Page<LandResponse> results = landService.searchLands(searchReq);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    /**
     * GET /api/lands/my-listings
     * Get all listings posted by the logged-in seller (PROTECTED)
     */
    @GetMapping("/my-listings")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getMyListings(
            Authentication authentication) {
        List<LandResponse> lands = landService.getMyListings(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Your listings", lands));
    }

    /**
     * POST /api/lands
     * Create new land listing (PROTECTED - Seller only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LandResponse>> createLand(
            @Valid @RequestBody LandRequest request,
            Authentication authentication) {
        try {
            LandResponse response = landService.createLand(
                    request, authentication.getName(), null);
            return ResponseEntity.ok(ApiResponse.success("Land listing created!", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/lands/{id}
     * Update land listing (PROTECTED - Owner only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LandResponse>> updateLand(
            @PathVariable Long id,
            @Valid @RequestBody LandRequest request,
            Authentication authentication) {
        try {
            LandResponse response = landService.updateLand(
                    id, request, authentication.getName(), null);
            return ResponseEntity.ok(ApiResponse.success("Land listing updated!", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /api/lands/{id}/sold
     * Mark land as sold (PROTECTED - Owner only)
     */
    @PatchMapping("/{id}/sold")
    public ResponseEntity<ApiResponse<LandResponse>> markAsSold(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            LandResponse response = landService.markAsSold(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Marked as sold!", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/lands/{id}
     * Delete/deactivate land listing (PROTECTED - Owner or Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLand(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            landService.deleteLand(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Listing removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
