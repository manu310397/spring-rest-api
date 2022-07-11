package com.manoj.springrest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="addresses")
@Getter
@Setter
public class AddressEntity implements Serializable {
    private static final long serialVersionUID = -5506826037803127244L;

    @Id
    @GeneratedValue
    private long id;

    @Column(length=30, nullable=false)
    private String addressId;

    @Column(length=15, nullable=false)
    private String city;

    @Column(length=15, nullable=false)
    private String country;

    @Column(length=100, nullable=false)
    private String streetName;

    @Column(length=15, nullable=false)
    private String postalCode;

    @Column(length=10, nullable=false)
    private String type;

    @ManyToOne
    @JoinColumn(name="user_id")
    @JsonIgnore
    private UserEntity userDetails;
}
