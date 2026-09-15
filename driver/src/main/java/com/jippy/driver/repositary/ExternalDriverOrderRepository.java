package com.jippy.driver.repositary;

import com.jippy.driver.entity.ExternalDriverOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalDriverOrderRepository extends JpaRepository<ExternalDriverOrder,Integer> {

    Optional<ExternalDriverOrder> findByExternalDeliveryId(String uberDeliveryId);
}
