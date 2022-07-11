package com.manoj.springrest.entity;

import com.manoj.springrest.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity implements Serializable {
    private static final long serialVersionUID = -2591552627963833757L;

    // db generated value not public:
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable=false)
    private String userId;

    @Column(nullable=false, length=50)
    private String firstName;

    @Column(nullable=false, length=50)
    private String lastName;

    @Column(nullable=false, length=120, unique = true)
    private String email;

    @Column(nullable=false)
    private String encryptedPassword;

    private String emailVerificationToken;

    @Column(nullable=false)
    private Boolean emailVerificationStatus = false;

    @OneToMany(mappedBy="userDetails", cascade=CascadeType.ALL)
    private List<AddressEntity> addresses;
//
//    //Cascade.PERSIST - If User is deleted the ROLE remains
//    @ManyToMany(cascade= {CascadeType.PERSIST}, fetch = FetchType.EAGER)
//    @JoinTable(name="users_roles",
//            joinColumns=@JoinColumn(name="users_id", referencedColumnName="id"),
//            inverseJoinColumns=@JoinColumn(name="roles_id", referencedColumnName="id"))
//    private Collection<RoleEntity>roles;

    public UserDTO toUserDTO() {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(this, userDTO);

        return userDTO;
    }
}
