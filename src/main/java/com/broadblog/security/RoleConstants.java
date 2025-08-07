package com.broadblog.security;

public class RoleConstants {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MODERATOR = "MODERATOR";
    
    // 角色前缀，Spring Security 要求角色名以 ROLE_ 开头
    public static final String SPRING_ROLE_USER = "ROLE_USER";
    public static final String SPRING_ROLE_ADMIN = "ROLE_ADMIN";
    public static final String SPRING_ROLE_MODERATOR = "ROLE_MODERATOR";
    
    private RoleConstants() {
        // 防止实例化
    }
} 