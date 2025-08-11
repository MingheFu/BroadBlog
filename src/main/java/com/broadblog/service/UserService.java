package com.broadblog.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.broadblog.entity.User;
import com.broadblog.repository.UserRepository;
import com.broadblog.security.RoleConstants;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CacheService cacheService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheService = cacheService;
    }

    // 用户注册
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
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
        
        User savedUser = userRepository.save(user);
        
        // 更新用户统计缓存
        updateUserStatsCache();
        
        return savedUser;
    }

    
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    
    @Cacheable(value = "users", key = "#id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 根据用户名获取用户
    @Cacheable(value = "users", key = "'username:' + #username")
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    
    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public User updateUser(User user) {
        User updatedUser = userRepository.save(user);
        
        // 清除相关缓存
        clearUserRelatedCache(user.getId());
        
        return updatedUser;
    }

    
    @CacheEvict(value = {"users", "userStats"}, allEntries = true)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        
        // 清除相关缓存
        clearUserRelatedCache(id);
        
        // 更新用户统计缓存
        updateUserStatsCache();
    }
    
    // 创建管理员用户
    @CacheEvict(value = {"users", "userStats", "admins"}, allEntries = true)
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
        
        User savedAdmin = userRepository.save(user);
        
        // 更新用户统计缓存
        updateUserStatsCache();
        
        return savedAdmin;
    }
    
    // 检查用户是否为管理员
    @Cacheable(value = "users", key = "'isAdmin:' + #userId")
    public boolean isAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return userOpt.get().getRoles().contains(RoleConstants.ROLE_ADMIN);
        }
        return false;
    }
    
    // 根据用户名检查是否为管理员
    @Cacheable(value = "users", key = "'isAdminUsername:' + #username")
    public boolean isAdminByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return userOpt.get().getRoles().contains(RoleConstants.ROLE_ADMIN);
        }
        return false;
    }
    
    // 升级用户为管理员
    @CacheEvict(value = {"users", "userStats", "admins"}, allEntries = true)
    public User promoteToAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        if (user.getRoles().contains(RoleConstants.ROLE_ADMIN)) {
            throw new RuntimeException("User is already an admin");
        }
        
        user.getRoles().add(RoleConstants.ROLE_ADMIN);
        User promotedUser = userRepository.save(user);
        
        // 更新用户统计缓存
        updateUserStatsCache();
        
        return promotedUser;
    }
    
    // 降级管理员为普通用户
    @CacheEvict(value = {"users", "userStats", "admins"}, allEntries = true)
    public User demoteFromAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        if (!user.getRoles().contains(RoleConstants.ROLE_ADMIN)) {
            throw new RuntimeException("User is not an admin");
        }
        
        user.getRoles().remove(RoleConstants.ROLE_ADMIN);
        User demotedUser = userRepository.save(user);
        
        // 更新用户统计缓存
        updateUserStatsCache();
        
        return demotedUser;
    }
    
    // 获取所有管理员
    @Cacheable(value = "admins", key = "'all'")
    public List<User> getAllAdmins() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles() != null && user.getRoles().contains(RoleConstants.ROLE_ADMIN))
            .toList();
    }
    
    // ========== 新增的Redis缓存功能 ==========
    
    /**
     * 用户登录时更新最后活跃时间
     */
    public void updateUserLastActive(Long userId) {
        String lastActiveKey = "user:" + userId + ":lastActive";
        cacheService.set(lastActiveKey, LocalDateTime.now(), java.time.Duration.ofDays(30));
        
        // 更新用户活跃统计
        String activeUsersKey = "stats:active_users";
        cacheService.sAdd(activeUsersKey, userId.toString());
        cacheService.expire(activeUsersKey, java.time.Duration.ofDays(1));
        
        System.out.println("user " + userId + " last active time updated");
    }
    
    /**
     * 获取用户最后活跃时间
     */
    public Optional<LocalDateTime> getUserLastActive(Long userId) {
        String lastActiveKey = "user:" + userId + ":lastActive";
        return cacheService.get(lastActiveKey, LocalDateTime.class);
    }
    
    /**
     * 获取在线用户数量
     */
    public Long getOnlineUserCount() {
        String activeUsersKey = "stats:active_users";
        return cacheService.sCard(activeUsersKey);
    }
    
    /**
     * 用户会话管理
     */
    public void createUserSession(Long userId, String sessionId) {
        String sessionKey = "session:" + sessionId;
        String userSessionsKey = "user:" + userId + ":sessions";
        
        // 存储会话信息
        cacheService.set(sessionKey, userId.toString(), java.time.Duration.ofHours(24));
        
        // 记录用户的所有会话
        cacheService.sAdd(userSessionsKey, sessionId);
        cacheService.expire(userSessionsKey, java.time.Duration.ofDays(7));
        
        System.out.println("user " + userId + " create new session: " + sessionId);
    }
    
    /**
     * 验证用户会话
     */
    public Optional<Long> validateUserSession(String sessionId) {
        String sessionKey = "session:" + sessionId;
        Optional<String> userIdStr = cacheService.get(sessionKey, String.class);
        
        if (userIdStr.isPresent()) {
            try {
                Long userId = Long.valueOf(userIdStr.get());
                return Optional.of(userId);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * 清除用户会话
     */
    public void clearUserSession(String sessionId) {
        String sessionKey = "session:" + sessionId;
        cacheService.delete(sessionKey);
        
        System.out.println("session " + sessionId + " cleared");
    }
    
    /**
     * 获取用户统计信息（从缓存）
     */
    public java.util.Map<String, Object> getUserStats() {
        String totalUsersKey = "stats:total_users";
        String totalAdminsKey = "stats:total_admins";
        String activeUsersKey = "stats:active_users";
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 从缓存获取统计信息
        Optional<Long> totalUsers = cacheService.get(totalUsersKey, Long.class);
        Optional<Long> totalAdmins = cacheService.get(totalAdminsKey, Long.class);
        Long activeUsers = getOnlineUserCount();
        
        stats.put("totalUsers", totalUsers.orElse(0L));
        stats.put("totalAdmins", totalAdmins.orElse(0L));
        stats.put("activeUsers", activeUsers);
        stats.put("regularUsers", totalUsers.orElse(0L) - totalAdmins.orElse(0L));
        
        return stats;
    }
    
    /**
     * 更新用户统计缓存
     */
    private void updateUserStatsCache() {
        String totalUsersKey = "stats:total_users";
        String totalAdminsKey = "stats:total_admins";
        
        // 获取实际统计数据
        List<User> allUsers = userRepository.findAll();
        List<User> admins = userRepository.findAll().stream()
            .filter(user -> user.getRoles() != null && user.getRoles().contains(RoleConstants.ROLE_ADMIN))
            .toList();
        
        // 更新缓存
        cacheService.set(totalUsersKey, (long) allUsers.size(), java.time.Duration.ofHours(1));
        cacheService.set(totalAdminsKey, (long) admins.size(), java.time.Duration.ofHours(1));
        
        System.out.println("user stats cache updated");
    }
    
    /**
     * 清除用户相关的缓存
     */
    private void clearUserRelatedCache(Long userId) {
        // 清除单个用户缓存
        cacheService.delete("users::" + userId);
        cacheService.delete("users::isAdmin:" + userId);
        
        // 清除用户名和邮箱缓存
        // 注意：这里需要知道用户名和邮箱才能清除，暂时清除所有
        cacheService.clearByPrefix("users::username:");
        cacheService.clearByPrefix("users::email:");
        
        // 清除管理员缓存
        cacheService.delete("admins::all");
        
        System.out.println("user " + userId + " related cache cleared");
    }
    
    /**
     * 预热用户缓存
     * 在应用启动时调用，预先加载用户数据到缓存
     */
    public void warmUpUserCache() {
        System.out.println("warm up user cache");
        
        // 预热所有用户
        getAllUsers();
        
        // 预热所有管理员
        getAllAdmins();
        
        // 更新用户统计缓存
        updateUserStatsCache();
        
        System.out.println("warm up user cache done");
    }
}
