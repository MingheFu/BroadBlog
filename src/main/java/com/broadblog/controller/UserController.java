package com.broadblog.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    // 获取当前用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // 检查权限：用户只能管理自己的信息，管理员可以管理所有用户
    private boolean hasPermission(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        // 如果是管理自己的信息，允许
        if (currentUserId.equals(targetUserId)) {
            return true;
        }
        // 如果是管理员，允许管理所有用户
        return userService.isAdmin(currentUserId);
    }

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            User user = userMapper.toEntity(userDTO);
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(userMapper.toDTO(savedUser));
        } catch (RuntimeException e) {
            // 处理用户名或邮箱一存在
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 获取所有用户（只有管理员可以访问）
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            Long currentUserId = getCurrentUserId();
            if (!userService.isAdmin(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. Admin role required."));
            }
            
            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            if (!hasPermission(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. You can only view your own profile."));
            }
            
            Optional<User> user = userService.getUserById(id);
            return user.map(u -> ResponseEntity.ok(userMapper.toDTO(u)))
                       .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
    }

    // 获取当前用户信息
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Long currentUserId = getCurrentUserId();
            Optional<User> user = userService.getUserById(currentUserId);
            return user.map(u -> ResponseEntity.ok(userMapper.toDTO(u)))
                       .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
    }

    // 根据用户名获取用户（只有管理员可以访问）
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            Long currentUserId = getCurrentUserId();
            if (!userService.isAdmin(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. Admin role required."));
            }
            
            Optional<User> user = userService.getUserByUsername(username);
            return user.map(u -> ResponseEntity.ok(userMapper.toDTO(u)))
                       .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            if (!hasPermission(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. You can only update your own profile."));
            }
            
            Optional<User> optionalUser = userService.getUserById(id);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            User user = optionalUser.get();
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setBio(userDTO.getBio());
            user.setAvatar(userDTO.getAvatar());
            
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!hasPermission(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. You can only delete your own account."));
            }
            
            // 防止删除自己
            Long currentUserId = getCurrentUserId();
            if (currentUserId.equals(id)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot delete your own account."));
            }
            
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
    }
} 