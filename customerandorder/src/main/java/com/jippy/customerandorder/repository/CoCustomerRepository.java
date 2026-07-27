

package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CoCustomerRepository extends JpaRepository<CoCustomer, Integer> {

    Optional<CoCustomer> findByEmail(String email);

    Optional<CoCustomer> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
    @Query(value = """
        SELECT *
        FROM jippy_customer_and_order.customer
        WHERE CURRENT_DATE - DATE(created_at)
        IN (15,30,60)
        """, nativeQuery = true)
    List<CoCustomer> findEligibleWelcomeCustomers();


}