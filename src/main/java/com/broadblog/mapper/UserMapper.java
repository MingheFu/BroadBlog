package com.broadblog.mapper;

import com.broadblog.dto.UserDTO;
import com.broadblog.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setAvatar(user.getAvatar());
        dto.setRoles(user.getRoles());
        return dto;
    }

    public User toEntity(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setBio(dto.getBio());
        user.setAvatar(dto.getAvatar());
        user.setRoles(dto.getRoles());
        
        return user;
    }
}
