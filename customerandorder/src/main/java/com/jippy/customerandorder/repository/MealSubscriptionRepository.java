package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoMealSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealSubscriptionRepository extends JpaRepository<CoMealSubscription, Integer> {

    List<CoMealSubscription> findBySubscriptionStatus(String subscriptionStatus);
}