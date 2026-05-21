package com.muyulu.mboard.service;

import com.muyulu.mboard.common.config.UploadProperties;
import com.muyulu.mboard.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final UploadProperties uploadProperties;

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Please select an image file");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Only jpg, jpeg, png, webp, gif are supported");
        }

        LocalDate today = LocalDate.now();
        String fileName = UUID.randomUUID() + "." + extension;
        Path baseDir = Paths.get(uploadProperties.getBaseDir()).toAbsolutePath().normalize();
        Path targetDir = baseDir.resolve("images")
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()));

        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(fileName).normalize();
            if (!targetFile.startsWith(baseDir)) {
                throw new BusinessException("Invalid upload path");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return buildPublicUrl(today, fileName);
        } catch (IOException exception) {
            throw new BusinessException("Image upload failed");
        }
    }

    private String buildPublicUrl(LocalDate today, String fileName) {
        String publicBaseUrl = uploadProperties.getPublicBaseUrl();
        String normalizedBase = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        return normalizedBase
                + "/images/"
                + today.getYear()
                + "/"
                + String.format("%02d", today.getMonthValue())
                + "/"
                + fileName;
    }

    private String extractExtension(String originalFilename) {
        String filename = StringUtils.hasText(originalFilename) ? originalFilename : "";
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            throw new BusinessException("Image suffix is required");
        }
        return filename.substring(index + 1).toLowerCase();
    }
}
