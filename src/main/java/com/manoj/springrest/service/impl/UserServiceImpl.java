package com.manoj.springrest.service.impl;

import com.manoj.springrest.dto.UserDTO;
import com.manoj.springrest.entity.UserEntity;
import com.manoj.springrest.repository.UserRepository;
import com.manoj.springrest.service.UserService;
import com.manoj.springrest.shared.Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    Utils utils;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository, Utils utils, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.utils = utils;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        UserEntity userFromDB = userRepository.findByEmail(userDTO.getEmail());

        if(userFromDB != null) throw new RuntimeException("User already exists");

        UserEntity userEntity = new UserEntity();
        UserDTO response = new UserDTO();

        BeanUtils.copyProperties(userDTO, userEntity);

        userEntity.setUserId(utils.generateUserId(25));
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));

        UserEntity savedUser = userRepository.save(userEntity);

        BeanUtils.copyProperties(savedUser, response);

        return response;
    }

    @Override
    public UserDTO findByEmail(String email) {
        UserDTO result = new UserDTO();

        UserEntity userFromDB = userRepository.findByEmail(email);

        if(userFromDB == null) throw new UsernameNotFoundException(email);

        BeanUtils.copyProperties(userFromDB, result);

        return result;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userFromDB = userRepository.findByEmail(email);

        if(userFromDB == null) throw new UsernameNotFoundException(email);

        return new User(userFromDB.getEmail(), userFromDB.getEncryptedPassword(), new ArrayList<>());
    }
}
