package com.landmarket.controller;

import com.landmarket.dto.ApiResponse;
import org.springframework.http.MediaType;
import com.landmarket.model.Land;
import com.landmarket.model.LandImage;
import com.landmarket.model.User;
import com.landmarket.repository.LandImageRepository;
import com.landmarket.repository.LandRepository;
import com.landmarket.repository.UserRepository;
import com.landmarket.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired private LandImageRepository landImageRepository;
    @Autowired private LandRepository landRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FileStorageService fileStorageService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── SERVE IMAGE ──────────────────────────────────────────────
    @GetMapping("/serve/{fileName:.+}")
    public ResponseEntity<Resource> serveImage(
            @PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .resolve(fileName)
                    .normalize();
            System.out.println("Serving file: " + filePath);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("File not found: " + filePath);
                return ResponseEntity.notFound().build();
            }
            String contentType = "image/jpeg";
            if (fileName.toLowerCase().endsWith(".png")) contentType = "image/png";
            if (fileName.toLowerCase().endsWith(".gif")) contentType = "image/gif";
            if (fileName.toLowerCase().endsWith(".webp")) contentType = "image/webp";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (Exception e) {
            System.out.println("Error serving image: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ── UPLOAD IMAGES ────────────────────────────────────────────
    @PostMapping(value = "/upload/{landId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadImages(
            @PathVariable Long landId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {

        try {

            // Check authentication
            if (authentication == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("User not logged in"));
            }

            // Get logged-in user
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Find land
            Land land = landRepository.findById(landId)
                    .orElseThrow(() -> new RuntimeException("Land not found"));

            // Check ownership
            if (!land.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Not authorized to upload images for this land"));
            }

            // Limit max images
            long existingCount = landImageRepository.countByLandId(landId);
            if (existingCount + files.size() > 3) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Maximum 3 images allowed per land"));
            }

            // Upload images
            for (int i = 0; i < files.size(); i++) {

                MultipartFile file = files.get(i);

                if (file.isEmpty()) continue;

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Only image files are allowed"));
                }

                // Save file
                String fileName = fileStorageService.saveFile(file);

                // Create image URL
                String imageUrl = "http://localhost:8080/api/images/serve/" + fileName;

                // Save in DB
                LandImage image = new LandImage();
                image.setLand(land);
                image.setImagePath(fileName);
                image.setImageUrl(imageUrl);
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
    // ── GET LAND IMAGES ──────────────────────────────────────────
    @GetMapping("/land/{landId}")
    public ResponseEntity<ApiResponse<?>> getLandImages(
            @PathVariable Long landId) {
        List<LandImage> images = landImageRepository
                .findByLandIdOrderByDisplayOrderAsc(landId);
        return ResponseEntity.ok(
                ApiResponse.success("Images fetched", images));
    }

    // ── DELETE IMAGE ─────────────────────────────────────────────
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
            fileStorageService.deleteFile(image.getImagePath());
            landImageRepository.delete(image);
            return ResponseEntity.ok(
                    ApiResponse.success("Image deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
