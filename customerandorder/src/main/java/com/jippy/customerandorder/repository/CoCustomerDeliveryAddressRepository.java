package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerDeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoCustomerDeliveryAddressRepository
        extends JpaRepository<CoCustomerDeliveryAddress, Integer> {
}