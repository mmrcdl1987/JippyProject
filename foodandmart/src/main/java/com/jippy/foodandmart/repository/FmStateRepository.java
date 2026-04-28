package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmStateRepository extends JpaRepository<FmState, Integer> {

    /**
     * Case-insensitive exact match on state_name.
     */
    @Query("SELECT s FROM FmState s WHERE LOWER(TRIM(s.stateName)) = LOWER(TRIM(:name))")
    Optional<FmState> findByStateNameIgnoreCase(@Param("name") String name);

    //    List<FmCity> findByStateId(Integer stateId);
}
