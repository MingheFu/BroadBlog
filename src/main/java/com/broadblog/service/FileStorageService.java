package com.broadblog.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.broadblog.config.FileStorageConfig;

@Service
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;
    private final Path uploadLocation;
    private final Path avatarLocation;
    private final Path postImageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
        this.uploadLocation = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        this.avatarLocation = uploadLocation.resolve(fileStorageConfig.getAvatarDir());
        this.postImageLocation = uploadLocation.resolve(fileStorageConfig.getPostImageDir());
        
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(uploadLocation);
            Files.createDirectories(avatarLocation);
            Files.createDirectories(postImageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories!", e);
        }
    }

    public String storeAvatarFile(MultipartFile file, Long userId) {
        return storeFile(file, avatarLocation, "avatar_" + userId + "_");
    }

    public String storePostImageFile(MultipartFile file, Long postId) {
        return storeFile(file, postImageLocation, "post_" + postId + "_");
    }

    private String storeFile(MultipartFile file, Path location, String prefix) {
        // 验证文件
        validateFile(file);

        // 生成唯一文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String fileName = prefix + timestamp + "_" + uniqueId + fileExtension;

        try {
            // 检查文件名
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file name: " + fileName);
            }

            // 复制文件到目标位置
            Path targetLocation = location.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new RuntimeException("File size exceeds maximum allowed size");
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(fileStorageConfig.getAllowedImageTypes()).contains(contentType)) {
            throw new RuntimeException("File type not allowed. Only images are permitted.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }

    public Path getAvatarPath(String fileName) {
        return avatarLocation.resolve(fileName);
    }

    public Path getPostImagePath(String fileName) {
        return postImageLocation.resolve(fileName);
    }

    public boolean deleteFile(String fileName, String type) {
        try {
            Path filePath;
            if ("avatar".equals(type)) {
                filePath = avatarLocation.resolve(fileName);
            } else if ("post-image".equals(type)) {
                filePath = postImageLocation.resolve(fileName);
            } else {
                return false;
            }
            
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}