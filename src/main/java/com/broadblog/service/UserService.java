package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.broadblog.entity.User;
import com.broadblog.repository.UserRepository;
import com.broadblog.security.RoleConstants;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 用户注册
    public User registerUser(User user) {
        
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
       
        // 设置默认角色
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of(RoleConstants.ROLE_USER));
        }
        
        return userRepository.save(user);
    }

    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 根据用户名获取用户
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    // 创建管理员用户
    public User createAdminUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置管理员角色
        user.setRoles(List.of(RoleConstants.ROLE_ADMIN));
        
        return userRepository.save(user);
    }
    
    // 检查用户是否为管理员
    public boolean isAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getRoles() != null && user.getRoles().contains(RoleConstants.ROLE_ADMIN);
        }
        return false;
    }
    
    // 检查用户是否为管理员（通过用户名）
    public boolean isAdminByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getRoles() != null && user.getRoles().contains(RoleConstants.ROLE_ADMIN);
        }
        return false;
    }
    
    // 升级用户为管理员
    public User promoteToAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRoles() == null) {
                user.setRoles(List.of(RoleConstants.ROLE_ADMIN));
            } else if (!user.getRoles().contains(RoleConstants.ROLE_ADMIN)) {
                user.getRoles().add(RoleConstants.ROLE_ADMIN);
            }
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }
    
    // 降级管理员为普通用户
    public User demoteFromAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRoles() != null) {
                user.getRoles().remove(RoleConstants.ROLE_ADMIN);
                if (user.getRoles().isEmpty()) {
                    user.setRoles(List.of(RoleConstants.ROLE_USER));
                }
            }
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }
    
    // 获取所有管理员
    public List<User> getAllAdmins() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles() != null && user.getRoles().contains(RoleConstants.ROLE_ADMIN))
            .toList();
    }
}
