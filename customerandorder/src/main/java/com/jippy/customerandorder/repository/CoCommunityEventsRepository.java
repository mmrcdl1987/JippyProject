package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCommunityEvents;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoCommunityEventsRepository extends JpaRepository<CoCommunityEvents,Integer> {

    Optional<CoCommunityEvents> findByCommunityIdAndEventTitle(Integer communityId,String eventTitle);
}
