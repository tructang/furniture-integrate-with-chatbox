package com.example.demochatbox.repository;

import com.example.demochatbox.model.Address;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByDefaultAddressDescIdDesc(Long userId);
}
