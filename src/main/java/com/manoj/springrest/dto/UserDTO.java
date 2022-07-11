package com.manoj.springrest.dto;

import com.manoj.springrest.ui.model.response.UserRest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO implements Serializable {
    private static final long serialVersionUID = -5974495421368688445L;

    private long id;
    // public user id:
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String encryptedPassword;
    private String emailVerificationToken;
    private Boolean emailVerificationStatus=false;
    private List<AddressDTO> addresses = new ArrayList<>();

    public UserRest toUserRest() {
        UserRest userRest = new UserRest();

        BeanUtils.copyProperties(this, userRest);

        return userRest;
    }
}
