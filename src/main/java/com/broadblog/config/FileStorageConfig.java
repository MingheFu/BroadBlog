package com.broadblog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {
    
    private String uploadDir = "uploads";
    private String avatarDir = "avatars";
    private String postImageDir = "post-images";
    private long maxFileSize = 5 * 1024 * 1024; // 5MB
    private String[] allowedImageTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    // Getters and Setters
    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getAvatarDir() {
        return avatarDir;
    }

    public void setAvatarDir(String avatarDir) {
        this.avatarDir = avatarDir;
    }

    public String getPostImageDir() {
        return postImageDir;
    }

    public void setPostImageDir(String postImageDir) {
        this.postImageDir = postImageDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String[] getAllowedImageTypes() {
        return allowedImageTypes;
    }

    public void setAllowedImageTypes(String[] allowedImageTypes) {
        this.allowedImageTypes = allowedImageTypes;
    }
}