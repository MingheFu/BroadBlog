package com.broadblog.security;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.broadblog.dto.AuthResponse;
import com.broadblog.dto.LoginRequest;
import com.broadblog.dto.UserDTO;
import com.broadblog.entity.User;
import com.broadblog.mapper.UserMapper;
import com.broadblog.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, UserMapper userMapper, 
                        JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 使用 Spring Security 的 AuthenticationManager 进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );

            // 认证成功，获取用户信息
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // 更新最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateUser(user);

            // 生成 Token
            String token = jwtTokenUtil.generateToken(user.getUsername());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

            // 转换为 DTO
            UserDTO userDTO = userMapper.toDTO(user);

            return ResponseEntity.ok(new AuthResponse(token, refreshToken, userDTO, "Login successful"));

        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse("Invalid username or password"));
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