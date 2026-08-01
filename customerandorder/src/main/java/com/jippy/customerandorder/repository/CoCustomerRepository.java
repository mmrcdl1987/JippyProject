

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

    @Query(value = """
            SELECT c.*
            FROM jippy_customer_and_order.customer c
            INNER JOIN jippy_customer_and_order.customer_status cs
                    ON c.customer_status_id = cs.customer_status_id
            WHERE cs.status_name = 'NEW'
              AND c.created_at <= NOW() - INTERVAL '24 HOURS'
            ORDER BY c.created_at ASC
            """, nativeQuery = true)
    List<CoCustomer> findProfileIncompleteCustomers();

    @Query(value = """
        SELECT *
        FROM jippy_customer_and_order.customer
        ORDER BY customer_id
        """, nativeQuery = true)
    List<CoCustomer> findAllCustomersForMealReminder();
}