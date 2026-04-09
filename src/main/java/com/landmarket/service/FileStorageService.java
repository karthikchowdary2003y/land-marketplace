package com.landmarket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        System.out.println("Saving to: " + uploadPath);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created directory: " + uploadPath);
        }

        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, 
                StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("File saved: " + fileName);
        return fileName;
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir)
                    .toAbsolutePath().resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + fileName);
        }
    }
}