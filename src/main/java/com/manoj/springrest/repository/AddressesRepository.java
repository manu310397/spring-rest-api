package com.manoj.springrest.repository;

import com.manoj.springrest.entity.AddressEntity;
import com.manoj.springrest.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressesRepository extends CrudRepository<AddressEntity, Long> {
    Iterable<AddressEntity> findAllByUserDetails(UserEntity userEntity);
    AddressEntity findByAddressId(String addressId);
}
