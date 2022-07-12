package com.manoj.springrest.service.impl;

import com.manoj.springrest.dto.AddressDTO;
import com.manoj.springrest.dto.UserDTO;
import com.manoj.springrest.entity.PasswordResetTokenEntity;
import com.manoj.springrest.entity.UserEntity;
import com.manoj.springrest.exceptions.UserServiceException;
import com.manoj.springrest.repository.PasswordResetTokenRepository;
import com.manoj.springrest.repository.UserRepository;
import com.manoj.springrest.service.UserService;
import com.manoj.springrest.shared.AmazonSES;
import com.manoj.springrest.shared.Utils;
import com.manoj.springrest.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    Utils utils;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    PasswordResetTokenRepository passwordResetTokenRepository;

    public UserServiceImpl(UserRepository userRepository,
                           Utils utils,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.utils = utils;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        UserEntity userFromDB = userRepository.findByEmail(userDTO.getEmail());

        if(userFromDB != null) throw new RuntimeException("User already exists");

        List<AddressDTO> addressList = userDTO.getAddresses().stream()
                .map(addressDTO -> {
                    addressDTO.setAddressId(utils.generateAddressId(25));
                    addressDTO.setUserDetails(userDTO);
                    return addressDTO;
                })
                .collect(Collectors.toList());

        userDTO.setAddresses(addressList);

        ModelMapper mapper = new ModelMapper();

        UserEntity userEntity = mapper.map(userDTO, UserEntity.class);

        String publicUserId = utils.generateUserId(25);
        userEntity.setUserId(publicUserId);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
        userEntity.setEmailVerificationStatus(Boolean.FALSE);

        UserEntity savedUser = userRepository.save(userEntity);

        UserDTO response = mapper.map(savedUser, UserDTO.class);

        new AmazonSES().sendVerificationEmail(response);

        return response;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
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

        return new User(userFromDB.getEmail(), userFromDB.getEncryptedPassword(), userFromDB.getEmailVerificationStatus(),
        true, true, true, new ArrayList<>());
//        return new User(userFromDB.getEmail(), userFromDB.getEncryptedPassword(), new ArrayList<>());
    }

    @Override
    public UserDTO getUserByUserId(String userId) {
        UserDTO result = new UserDTO();
        UserEntity entity = userRepository.findByUserId(userId);

        if(entity == null) throw new UsernameNotFoundException(userId);

        new ModelMapper().map(entity, result);

        return result;
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        UserDTO result = new UserDTO();
        UserEntity entity = userRepository.findByUserId(userId);

        if(entity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        entity.setFirstName(userDTO.getFirstName());
        entity.setLastName(userDTO.getLastName());

        UserEntity updatedUser = userRepository.save(entity);

        BeanUtils.copyProperties(updatedUser, result);

        return result;
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity entity = userRepository.findByUserId(userId);

        if(entity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userRepository.delete(entity);
    }

    @Override
    public List<UserDTO> getUsers(int page, int limit) {
        List<UserDTO> result;

        Pageable pageableRequest = PageRequest.of(page, limit);

        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);

        List<UserEntity> usersFromDB =usersPage.getContent();

        result = usersFromDB.stream()
                .map(userEntity -> userEntity.toUserDTO())
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public boolean verifyEmail(String token) {

        UserEntity entity = userRepository.findByEmailVerificationToken(token);

        if(entity != null) {
            boolean hasTokenExpired = utils.hasTokenExpired(token);
            if(!hasTokenExpired) {
                entity.setEmailVerificationToken(null);
                entity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(entity);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean requestPasswordReset(String email) {

        UserEntity entity = userRepository.findByEmail(email);

        if(entity == null) return false;

        String passwordResetToken = utils.generatePasswordResetToken(entity.getUserId());

        PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
        passwordResetTokenEntity.setToken(passwordResetToken);
        passwordResetTokenEntity.setUserDetails(entity);
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        return new AmazonSES().sendPasswordResetRequest(entity.getFirstName(), entity.getEmail(), passwordResetToken);

    }

    @Override
    public boolean resetPassword(String token, String password) {
        if(utils.hasTokenExpired(token)) return false;

        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

        System.out.println(token);
        System.out.println(passwordResetTokenEntity.getToken());

        if(passwordResetTokenEntity == null) return false;

        String encryptedPassword = bCryptPasswordEncoder.encode(password);

        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encryptedPassword);
        UserEntity savedUser = userRepository.save(userEntity);

        passwordResetTokenRepository.delete(passwordResetTokenEntity);

        if(savedUser != null) return true;
        return false;
    }
}
