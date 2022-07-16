package com.manoj.springrest.ui.controller;

import com.manoj.springrest.dto.AddressDTO;
import com.manoj.springrest.dto.UserDTO;
import com.manoj.springrest.exceptions.UserServiceException;
import com.manoj.springrest.service.AddressesService;
import com.manoj.springrest.service.UserService;
import com.manoj.springrest.ui.model.request.PasswordResetModel;
import com.manoj.springrest.ui.model.request.PasswordResetRequestModel;
import com.manoj.springrest.ui.model.request.UserDetailsRequestModel;
import com.manoj.springrest.ui.model.response.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
//@CrossOrigin("*")
public class UserController {

    UserService userService;
    AddressesService addressesService;

    public UserController(UserService userService, AddressesService addressesService) {
        this.userService = userService;
        this.addressesService = addressesService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetailsRequestModel) {

        if(userDetailsRequestModel.getLastName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

//        BeanUtils.copyProperties(userDetailsRequestModel, userDTO);
        ModelMapper mapper = new ModelMapper();
        UserDTO userDTO = mapper.map(userDetailsRequestModel, UserDTO.class);

        UserDTO savedUser = userService.createUser(userDTO);
        UserRest response = mapper.map(savedUser, UserRest.class);

        return response;
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest getUser(@PathVariable String id) {
        UserRest response = new UserRest();

        UserDTO userDTO = userService.getUserByUserId(id);

        BeanUtils.copyProperties(userDTO, response);

        return response;
    }

    @PutMapping("/{id}")
    public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetailsRequestModel) {
        UserRest response = new UserRest();

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(userDetailsRequestModel, userDTO);

        UserDTO updatedUser = userService.updateUser(id, userDTO);
        BeanUtils.copyProperties(updatedUser, response);

        return response;
    }

    @DeleteMapping("/{id}")
    public OperationStatusModel deleteUser(@PathVariable String id) {
        OperationStatusModel response = new OperationStatusModel();

        response.setOperationName(RequestOperationName.DELETE.name());

        userService.deleteUser(id);

        response.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return response;
    }

    @ApiImplicitParams(
            @ApiImplicitParam(name = "authorization", value = "Bearer JWT token", paramType = "header")
    )
    @ApiOperation(value = "Get User Details Web service end point")
    @GetMapping
    public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "limit", defaultValue = "25") int limit) {
        List<UserRest> response;

        List<UserDTO> userDTOS = userService.getUsers(page, limit);

        response = userDTOS.stream()
                .map(userDTO -> userDTO.toUserRest())
                .collect(Collectors.toList());

        return response;
    }

    @GetMapping(value = "/{id}/addresses")
    public List<AddressRest> getAddressList(@PathVariable String id) {
        List<AddressRest> response;

        List<AddressDTO> addressDTOS = addressesService.getAddresses(id);

        Type listType = new TypeToken<List<AddressRest>>() {}.getType();
        response = new ModelMapper().map(addressDTOS, listType);

        return response;
    }

    @GetMapping(path = "/{userId}/addresses/{addressId}")
    public AddressRest getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
        AddressRest response;
        AddressDTO addressDTO;
        addressDTO = addressesService.getAddress(addressId);

        response = new ModelMapper().map(addressDTO, AddressRest.class);

        Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
        Link addressesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddressList(userId))
//                .slash(userId)
//                .slash("addresses")
                .withRel("addresses");
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn((UserController.class)).getUserAddress(userId, addressId))
//                .slash(userId)
//                .slash("addresses")
//                .slash(addressId)
                .withSelfRel();

        response.add(userLink);
        response.add(addressesLink);
        response.add(selfLink);

        return response;
    }

    @GetMapping(path = "/email-verification")
    public OperationStatusModel verifyEmail(@RequestParam(value = "token") String token) {
        OperationStatusModel response = new OperationStatusModel();

        response.setOperationName(RequestOperationName.VERIFY_EMAIL.name());

        boolean isVerified = userService.verifyEmail(token);

        if(isVerified) response.setOperationResult(RequestOperationStatus.SUCCESS.name());
        else response.setOperationResult(RequestOperationStatus.ERROR.name());

        return response;
    }

    @PostMapping(path = "/password-reset-request")
    public OperationStatusModel requestPasswordReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
        OperationStatusModel response = new OperationStatusModel();

        boolean result = userService.requestPasswordReset(passwordResetRequestModel.getEmail());

        response.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());

        if(result) response.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return response;
    }

    @PostMapping(path = "/password-reset")
    public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
        OperationStatusModel response = new OperationStatusModel();

        boolean result = userService.resetPassword(passwordResetModel.getToken(), passwordResetModel.getPassword());

        response.setOperationName(RequestOperationName.RESET_PASSWORD.name());

        if(result) response.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return response;
    }
}
