package com.broadblog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.broadblog.entity.User;
import com.broadblog.repository.UserRepository;

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
            user.setRoles(List.of("USER"));
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

    
    public void sayHello() {
        System.out.println("Hello from UserService!");
    }
}
