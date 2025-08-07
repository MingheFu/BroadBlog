package com.broadblog.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.dto.UserDTO;
import com.broadblog.entity.User;
import com.broadblog.mapper.UserMapper;
import com.broadblog.security.CustomUserDetails;
import com.broadblog.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public AdminController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    // 获取当前用户ID（必须是管理员）
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // 检查当前用户是否为管理员
    private void checkAdminPermission() {
        Long currentUserId = getCurrentUserId();
        if (!userService.isAdmin(currentUserId)) {
            throw new RuntimeException("Access denied. Admin role required.");
        }
    }

    // 获取所有用户（管理员专用）
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            checkAdminPermission();
            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 获取所有管理员
    @GetMapping("/admins")
    public ResponseEntity<List<UserDTO>> getAllAdmins() {
        try {
            checkAdminPermission();
            List<User> admins = userService.getAllAdmins();
            List<UserDTO> adminDTOs = admins.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(adminDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 创建管理员用户
    @PostMapping("/users/admin")
    public ResponseEntity<?> createAdminUser(@RequestBody UserDTO userDTO) {
        try {
            checkAdminPermission();
            User user = userMapper.toEntity(userDTO);
            User createdAdmin = userService.createAdminUser(user);
            return ResponseEntity.ok(userMapper.toDTO(createdAdmin));
        } catch (RuntimeException e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 升级用户为管理员
    @PutMapping("/users/{userId}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long userId) {
        try {
            checkAdminPermission();
            User promotedUser = userService.promoteToAdmin(userId);
            return ResponseEntity.ok(userMapper.toDTO(promotedUser));
        } catch (RuntimeException e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 降级管理员为普通用户
    @PutMapping("/users/{userId}/demote")
    public ResponseEntity<?> demoteFromAdmin(@PathVariable Long userId) {
        try {
            checkAdminPermission();
            // 防止降级自己
            Long currentUserId = getCurrentUserId();
            if (currentUserId.equals(userId)) {
                Map<String, String> error = Map.of("error", "Cannot demote yourself");
                return ResponseEntity.badRequest().body(error);
            }
            
            User demotedUser = userService.demoteFromAdmin(userId);
            return ResponseEntity.ok(userMapper.toDTO(demotedUser));
        } catch (RuntimeException e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 删除用户（管理员专用）
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            checkAdminPermission();
            // 防止删除自己
            Long currentUserId = getCurrentUserId();
            if (currentUserId.equals(userId)) {
                Map<String, String> error = Map.of("error", "Cannot delete yourself");
                return ResponseEntity.badRequest().body(error);
            }
            
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 获取系统统计信息
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            checkAdminPermission();
            List<User> allUsers = userService.getAllUsers();
            List<User> admins = userService.getAllAdmins();
            
            Map<String, Object> stats = Map.of(
                "totalUsers", allUsers.size(),
                "totalAdmins", admins.size(),
                "regularUsers", allUsers.size() - admins.size()
            );
            
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
} 