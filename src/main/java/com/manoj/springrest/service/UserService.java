package com.manoj.springrest.service;

import com.manoj.springrest.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserByEmail(String email);
    UserDTO getUserByUserId(String userId);
    UserDTO updateUser(String userId, UserDTO userDTO);
    void deleteUser(String userId);
    List<UserDTO> getUsers(int page, int limit);
    boolean verifyEmail(String token);
}
