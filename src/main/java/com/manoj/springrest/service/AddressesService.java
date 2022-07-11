package com.manoj.springrest.service;

import com.manoj.springrest.dto.AddressDTO;

import java.util.List;

public interface AddressesService {
    List<AddressDTO> getAddresses(String userId);
    AddressDTO getAddress(String addressId);
}
