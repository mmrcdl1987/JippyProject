

package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CoCustomerRepository extends JpaRepository<CoCustomer, Integer> {

    Optional<CoCustomer> findByEmail(String email);

    Optional<CoCustomer> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

}