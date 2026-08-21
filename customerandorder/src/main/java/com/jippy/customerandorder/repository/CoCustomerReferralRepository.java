package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoCustomerReferralRepository extends JpaRepository<CoCustomerReferral, Integer> {

    Optional<CoCustomerReferral> findByRefereeCustomerIdAndReferralStatus(Integer refereeCustomerId, String referralStatus);

    Optional<CoCustomerReferral> findByRefereeCustomerId(Integer refereeCustomerId);

    boolean existsByRefereeCustomerId(Integer refereeCustomerId);

    List<CoCustomerReferral> findByReferrerCustomerIdAndReferralStatus(Integer referrerCustomerId, String referralStatus);
}