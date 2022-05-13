package com.manoj.springrest.ui.controller;

import com.manoj.springrest.dto.UserDTO;
import com.manoj.springrest.service.UserService;
import com.manoj.springrest.ui.model.request.UserDetailsRequestModel;
import com.manoj.springrest.ui.model.response.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetailsRequestModel) {
        UserRest response = new UserRest();

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(userDetailsRequestModel, userDTO);

        UserDTO savedUser = userService.createUser(userDTO);
        BeanUtils.copyProperties(savedUser, response);

        return response;
    }

    @GetMapping
    public String getUser() {
        return "Get user request";
    }
}
