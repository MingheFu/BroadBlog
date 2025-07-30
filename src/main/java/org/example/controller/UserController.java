package org.example.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 获取所有用户
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> ResponseEntity.ok(userMapper.toDTO(u)))
                   .orElse(ResponseEntity.notFound().build());
    }

    // 根据用户名获取用户
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(u -> ResponseEntity.ok(userMapper.toDTO(u)))
                   .orElse(ResponseEntity.notFound().build());
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
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
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
} 