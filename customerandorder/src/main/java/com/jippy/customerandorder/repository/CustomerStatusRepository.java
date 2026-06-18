package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerStatusRepository extends JpaRepository<CustomerStatus, Integer> {

    Optional<CustomerStatus> findByStatusName(String statusName);
}