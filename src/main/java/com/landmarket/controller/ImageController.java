package com.landmarket.controller;

import com.landmarket.dto.ApiResponse;
import com.landmarket.model.Land;
import com.landmarket.model.LandImage;
import com.landmarket.model.User;
import com.landmarket.repository.LandImageRepository;
import com.landmarket.repository.LandRepository;
import com.landmarket.repository.UserRepository;
import com.landmarket.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired private LandImageRepository landImageRepository;
    @Autowired private LandRepository landRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CloudinaryService cloudinaryService; // ← Cloudinary instead of FileStorage

    // ── UPLOAD IMAGES ─────────────────────────────────────────────
    @PostMapping(value = "/upload/{landId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadImages(
            @PathVariable Long landId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {

        try {
            if (authentication == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("User not logged in"));
            }

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Land land = landRepository.findById(landId)
                    .orElseThrow(() -> new RuntimeException("Land not found"));

            if (!land.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Not authorized"));
            }

            long existingCount = landImageRepository.countByLandId(landId);
            if (existingCount + files.size() > 3) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Maximum 3 images allowed per land"));
            }

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file.isEmpty()) continue;

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Only image files allowed"));
                }

                // ✅ Upload to Cloudinary - permanent URL
                String imageUrl = cloudinaryService.uploadImage(file);

                LandImage image = new LandImage();
                image.setLand(land);
                image.setImagePath(imageUrl);  // store full cloudinary URL
                image.setImageUrl(imageUrl);   // permanent cloudinary URL
                image.setDisplayOrder((int) existingCount + i);

                landImageRepository.save(image);
            }

            return ResponseEntity.ok(
                    ApiResponse.success("Images uploaded successfully!", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── GET LAND IMAGES ───────────────────────────────────────────
    @GetMapping("/land/{landId}")
    public ResponseEntity<ApiResponse<?>> getLandImages(@PathVariable Long landId) {
        List<LandImage> images = landImageRepository
                .findByLandIdOrderByDisplayOrderAsc(landId);
        return ResponseEntity.ok(ApiResponse.success("Images fetched", images));
    }

    // ── DELETE IMAGE ──────────────────────────────────────────────
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteImage(
            @PathVariable Long imageId,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LandImage image = landImageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found"));

            if (!image.getLand().getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Not authorized"));
            }

            // Extract public_id from cloudinary URL and delete
            String imageUrl = image.getImageUrl();
            String publicId = extractPublicId(imageUrl);
            cloudinaryService.deleteImage(publicId);

            landImageRepository.delete(image);
            return ResponseEntity.ok(ApiResponse.success("Image deleted", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Extract Cloudinary public_id from URL
    // e.g. https://res.cloudinary.com/xxx/image/upload/v123/landmart/abc.jpg → landmart/abc
    private String extractPublicId(String url) {
        try {
            String[] parts = url.split("/upload/");
            String afterUpload = parts[1]; // v123/landmart/abc.jpg
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            return withoutVersion.substring(0, withoutVersion.lastIndexOf('.'));
        } catch (Exception e) {
            return url;
        }
    }
}