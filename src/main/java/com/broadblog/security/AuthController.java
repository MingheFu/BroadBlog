package com.broadblog.security;

import com.broadblog.dto.AuthResponse;
import com.broadblog.dto.LoginRequest;
import com.broadblog.dto.UserDTO;
import com.broadblog.entity.User;
import com.broadblog.service.UserService;
import com.broadblog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthController(UserService userService, UserMapper userMapper, 
                        PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 查找用户
            User user = userService.getUserByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // 验证密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse("Invalid username or password"));
            }

            // 检查账户状态
            if (!user.isEnabled()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse("Account is disabled"));
            }

            // 更新最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateUser(user);

            // 生成 Token（只在登录时生成）
            String token = jwtTokenUtil.generateToken(user.getUsername());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

            // 转换为 DTO
            UserDTO userDTO = userMapper.toDTO(user);

            return ResponseEntity.ok(new AuthResponse(token, refreshToken, userDTO, "Login successful"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        try {
            // 验证刷新 Token
            if (jwtTokenUtil.validateRefreshToken(refreshToken)) {
                String username = jwtTokenUtil.extractUsername(refreshToken);
                
                // 生成新的 Token
                String newToken = jwtTokenUtil.generateToken(username);
                String newRefreshToken = jwtTokenUtil.generateRefreshToken(username);

                return ResponseEntity.ok(new AuthResponse(newToken, newRefreshToken, null, "Token refreshed"));
            } else {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse("Invalid refresh token"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse("Token refresh failed: " + e.getMessage()));
        }
    }
} 