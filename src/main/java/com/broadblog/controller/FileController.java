package com.broadblog.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.broadblog.entity.User;
import com.broadblog.security.CustomUserDetails;
import com.broadblog.service.FileStorageService;
import com.broadblog.service.UserService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Autowired
    public FileController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    // 获取当前登录用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // 上传用户头像
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = getCurrentUserId();
            
            // 存储文件
            String fileName = fileStorageService.storeAvatarFile(file, userId);
            
            // 更新用户头像路径
            User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // 删除旧头像文件（如果存在）
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                fileStorageService.deleteFile(user.getAvatar(), "avatar");
            }
            
            user.setAvatar(fileName);
            userService.updateUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("fileName", fileName);
            response.put("downloadUrl", "/api/files/avatar/" + fileName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 上传帖子图片
    @PostMapping("/post-image")
    public ResponseEntity<Map<String, Object>> uploadPostImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "postId", required = false) Long postId) {
        try {
            // 如果没有指定 postId，使用当前用户ID作为临时标识
            if (postId == null) {
                postId = getCurrentUserId();
            }
            
            String fileName = fileStorageService.storePostImageFile(file, postId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("fileName", fileName);
            response.put("downloadUrl", "/api/files/post-image/" + fileName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 下载用户头像
    @GetMapping("/avatar/{fileName:.+}")
    public ResponseEntity<Resource> downloadAvatar(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageService.getAvatarPath(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = getContentType(fileName);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 下载帖子图片
    @GetMapping("/post-image/{fileName:.+}")
    public ResponseEntity<Resource> downloadPostImage(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageService.getPostImagePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = getContentType(fileName);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 删除文件
    @DeleteMapping("/{type}/{fileName:.+}")
    public ResponseEntity<Map<String, String>> deleteFile(
            @PathVariable String type, 
            @PathVariable String fileName) {
        try {
            Long userId = getCurrentUserId();
            
            // 权限验证：只能删除自己的文件
            if ("avatar".equals(type)) {
                User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                if (!fileName.equals(user.getAvatar())) {
                    return ResponseEntity.status(403).build();
                }
            }
            
            boolean deleted = fileStorageService.deleteFile(fileName, type);
            
            Map<String, String> response = new HashMap<>();
            if (deleted) {
                response.put("message", "File deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "File not found or could not be deleted");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}