package com.example.demochatbox.service;

import com.example.demochatbox.dto.AddressDtos.AddressRequest;
import com.example.demochatbox.dto.AddressDtos.AddressResponse;
import com.example.demochatbox.model.Address;
import com.example.demochatbox.model.UserAccount;
import com.example.demochatbox.repository.AddressRepository;
import com.example.demochatbox.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse save(AddressRequest request) {
        UserAccount user = userAccountRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));
        if (request.defaultAddress()) {
            addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(request.userId())
                    .forEach(address -> address.setDefaultAddress(false));
        }
        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setLine1(request.line1());
        address.setDistrict(request.district());
        address.setCity(request.city());
        address.setDefaultAddress(request.defaultAddress());
        return toResponse(addressRepository.save(address));
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(address.getId(), address.getRecipientName(), address.getPhone(), address.getLine1(),
                address.getDistrict(), address.getCity(), address.isDefaultAddress());
    }
}
