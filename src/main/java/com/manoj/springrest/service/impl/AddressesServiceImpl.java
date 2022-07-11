package com.manoj.springrest.service.impl;

import com.manoj.springrest.dto.AddressDTO;
import com.manoj.springrest.entity.AddressEntity;
import com.manoj.springrest.entity.UserEntity;
import com.manoj.springrest.exceptions.UserServiceException;
import com.manoj.springrest.repository.AddressesRepository;
import com.manoj.springrest.repository.UserRepository;
import com.manoj.springrest.service.AddressesService;
import com.manoj.springrest.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressesServiceImpl implements AddressesService {

    UserRepository userRepository;
    AddressesRepository addressesRepository;

    AddressesServiceImpl(UserRepository userRepository, AddressesRepository addressesRepository) {
        this.userRepository = userRepository;
        this.addressesRepository = addressesRepository;
    }

    @Override
    public List<AddressDTO> getAddresses(String userId) {
        List<AddressDTO> result = new ArrayList<>();

        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Iterable<AddressEntity> addresses = addressesRepository.findAllByUserDetails(userEntity);

        addresses.forEach(addressEntity -> result.add(new ModelMapper().map(addressEntity, AddressDTO.class)));

        return result;
    }

    @Override
    public AddressDTO getAddress(String addressId) {
        AddressEntity addressEntity = addressesRepository.findByAddressId(addressId);

        if(addressEntity != null) {
            return new ModelMapper().map(addressEntity, AddressDTO.class);
        }

        return null;
    }
}
