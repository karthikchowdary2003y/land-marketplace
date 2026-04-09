package com.landmarket.service;

import com.landmarket.dto.LandRequest;
import com.landmarket.dto.LandResponse;
import com.landmarket.dto.LandSearchRequest;
import com.landmarket.model.Land;
import com.landmarket.model.User;
import com.landmarket.repository.LandRepository;
import com.landmarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LandService {

    @Autowired private LandRepository landRepository;
    @Autowired private UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ============ CREATE LAND LISTING ============
    @Transactional
    public LandResponse createLand(LandRequest request, String ownerEmail,
                                   List<MultipartFile> images) throws IOException {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (owner.getRole() != User.Role.SELLER && owner.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only sellers can post land listings");
        }

        Land land = Land.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .areaInAcres(request.getAreaInAcres())
                .landType(request.getLandType())
                .status(request.getStatus() != null ? request.getStatus() : Land.LandStatus.AVAILABLE)
                .surveyNumber(request.getSurveyNumber())
                .documentNumber(request.getDocumentNumber())
                .roadAccess(request.getRoadAccess())
                .waterSource(request.isWaterSource())
                .electricity(request.isElectricity())
                .fencing(request.isFencing())
                .owner(owner)
                .active(true)
                .build();

        // Handle image uploads
        if (images != null && !images.isEmpty()) {
            List<String> imagePaths = saveImages(images, owner.getId());
            land.setImages(String.join(",", imagePaths));
        }

        landRepository.save(land);
        return mapToResponse(land);
    }

    // ============ GET ALL LANDS (Public) ============
    public Page<LandResponse> getAllLands(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return landRepository.findByActiveTrue(pageable).map(this::mapToResponse);
    }

    // ============ GET SINGLE LAND (Public - shows owner contact) ============
    @Transactional
    public LandResponse getLandById(Long id) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));

        if (!land.isActive()) {
            throw new RuntimeException("Land listing is not available");
        }

        // Increment view count
        land.setViewCount(land.getViewCount() + 1);
        landRepository.save(land);

        return mapToResponse(land);
    }

    // ============ SEARCH LANDS ============
    public Page<LandResponse> searchLands(LandSearchRequest searchReq) {
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(searchReq.getSortDir()) ?
                        Sort.Direction.ASC : Sort.Direction.DESC,
                searchReq.getSortBy()
        );
        Pageable pageable = PageRequest.of(searchReq.getPage(), searchReq.getSize(), sort);

        // If keyword given, use text search
        if (searchReq.getKeyword() != null && !searchReq.getKeyword().isBlank()) {
            return landRepository.searchByKeyword(searchReq.getKeyword(), pageable)
                    .map(this::mapToResponse);
        }

        // Advanced filter search
        return landRepository.searchLands(
                searchReq.getCity(),
                searchReq.getState(),
                searchReq.getLandType(),
                searchReq.getMinPrice(),
                searchReq.getMaxPrice(),
                searchReq.getMinArea(),
                searchReq.getMaxArea(),
                searchReq.getStatus(),
                pageable
        ).map(this::mapToResponse);
    }

    // ============ MY LISTINGS (Seller view) ============
    public List<LandResponse> getMyListings(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return landRepository.findByOwnerAndActiveTrue(owner)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ============ UPDATE LAND ============
    @Transactional
    public LandResponse updateLand(Long id, LandRequest request, String ownerEmail,
                                   List<MultipartFile> newImages) throws IOException {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found"));

        if (!land.getOwner().getEmail().equals(ownerEmail)) {
            throw new AccessDeniedException("You can only edit your own listings");
        }

        land.setTitle(request.getTitle());
        land.setDescription(request.getDescription());
        land.setPrice(request.getPrice());
        land.setAddress(request.getAddress());
        land.setCity(request.getCity());
        land.setState(request.getState());
        land.setPincode(request.getPincode());
        land.setLatitude(request.getLatitude());
        land.setLongitude(request.getLongitude());
        land.setAreaInAcres(request.getAreaInAcres());
        land.setLandType(request.getLandType());
        land.setStatus(request.getStatus());
        land.setSurveyNumber(request.getSurveyNumber());
        land.setRoadAccess(request.getRoadAccess());
        land.setWaterSource(request.isWaterSource());
        land.setElectricity(request.isElectricity());
        land.setFencing(request.isFencing());

        if (newImages != null && !newImages.isEmpty()) {
            List<String> imagePaths = saveImages(newImages, land.getOwner().getId());
            land.setImages(String.join(",", imagePaths));
        }

        landRepository.save(land);
        return mapToResponse(land);
    }

    // ============ DELETE LAND ============
    @Transactional
    public void deleteLand(Long id, String ownerEmail) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found"));

        User currentUser = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!land.getOwner().getEmail().equals(ownerEmail)
                && currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("You can only delete your own listings");
        }

        land.setActive(false);
        landRepository.save(land);
    }

    // ============ MARK AS SOLD ============
    @Transactional
    public LandResponse markAsSold(Long id, String ownerEmail) {
        Land land = landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found"));

        if (!land.getOwner().getEmail().equals(ownerEmail)) {
            throw new AccessDeniedException("You can only update your own listings");
        }

        land.setStatus(Land.LandStatus.SOLD);
        landRepository.save(land);
        return mapToResponse(land);
    }

    // ============ IMAGE UPLOAD ============
    private List<String> saveImages(List<MultipartFile> images, Long userId) throws IOException {
        List<String> savedPaths = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir, String.valueOf(userId));
        Files.createDirectories(uploadPath);

        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                savedPaths.add("/uploads/land-images/" + userId + "/" + fileName);
            }
        }
        return savedPaths;
    }

    // ============ MAPPER ============
    public LandResponse mapToResponse(Land land) {
        LandResponse response = new LandResponse();
        response.setId(land.getId());
        response.setTitle(land.getTitle());
        response.setDescription(land.getDescription());
        response.setPrice(land.getPrice());
        response.setAddress(land.getAddress());
        response.setCity(land.getCity());
        response.setState(land.getState());
        response.setPincode(land.getPincode());
        response.setLatitude(land.getLatitude());
        response.setLongitude(land.getLongitude());
        response.setAreaInAcres(land.getAreaInAcres());
        response.setLandType(land.getLandType());
        response.setStatus(land.getStatus());
        response.setSurveyNumber(land.getSurveyNumber());
        response.setDocumentVerified(land.isDocumentVerified());
        response.setRoadAccess(land.getRoadAccess());
        response.setWaterSource(land.isWaterSource());
        response.setElectricity(land.isElectricity());
        response.setFencing(land.isFencing());
        response.setViewCount(land.getViewCount());
        response.setCreatedAt(land.getCreatedAt());

        // Parse image paths
        if (land.getImages() != null && !land.getImages().isBlank()) {
            response.setImages(Arrays.asList(land.getImages().split(",")));
        } else {
            response.setImages(new ArrayList<>());
        }

        // ** KEY FEATURE: Direct owner contact - No Middleman **
        LandResponse.OwnerInfo ownerInfo = new LandResponse.OwnerInfo();
        ownerInfo.setId(land.getOwner().getId());
        ownerInfo.setFullName(land.getOwner().getFullName());
        ownerInfo.setPhone(land.getOwner().getPhone());     // Direct call!
        ownerInfo.setEmail(land.getOwner().getEmail());     // Direct email!
        ownerInfo.setCity(land.getOwner().getCity());
        ownerInfo.setState(land.getOwner().getState());
        ownerInfo.setProfileImage(land.getOwner().getProfileImage());
        response.setOwner(ownerInfo);

        return response;
    }
}
