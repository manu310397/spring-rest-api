package com.manoj.springrest.ui.controller;

import com.manoj.springrest.dto.UserDTO;
import com.manoj.springrest.exceptions.UserServiceException;
import com.manoj.springrest.service.UserService;
import com.manoj.springrest.ui.model.request.UserDetailsRequestModel;
import com.manoj.springrest.ui.model.response.ErrorMessages;
import com.manoj.springrest.ui.model.response.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetailsRequestModel) {

        if(userDetailsRequestModel.getLastName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        UserRest response = new UserRest();

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(userDetailsRequestModel, userDTO);

        UserDTO savedUser = userService.createUser(userDTO);
        BeanUtils.copyProperties(savedUser, response);

        return response;
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest getUser(@PathVariable String id) {
        UserRest response = new UserRest();

        UserDTO userDTO = userService.getUserByUserId(id);

        BeanUtils.copyProperties(userDTO, response);

        return response;
    }
}
