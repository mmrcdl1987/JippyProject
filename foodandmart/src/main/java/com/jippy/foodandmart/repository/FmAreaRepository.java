package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmAreaRepository extends JpaRepository<FmArea, Integer> {

    /**
     * Case-insensitive exact match on area_name.
     *
     * <p>Used during outlet bulk upload to resolve the human-readable area name
     * supplied in the ZipCode column to the integer area_id FK stored in the
     * address table.</p>
     */
//    @Query("SELECT a FROM Area a WHERE LOWER(TRIM(a.areaName)) = LOWER(TRIM(:name))")
//    Optional<FmArea> findByAreaNameIgnoreCase(@Param("name") String name);

    @Query("SELECT a FROM FmArea a WHERE LOWER(TRIM(a.areaName)) = LOWER(TRIM(:name))")
    Optional<FmArea> findByAreaNameIgnoreCase(@Param("name") String name);

    List<FmArea> findByCityId(Integer cityId);

    @Query("""
       SELECT a
       FROM FmArea a
       WHERE LOWER(TRIM(a.areaName)) = LOWER(TRIM(:areaName))
       AND a.cityId = :cityId
       """)
    Optional<FmArea> findByAreaNameIgnoreCaseAndCityId(@Param("areaName") String areaName,
            @Param("cityId") Integer cityId);

    Optional<FmArea> findByAreaId(Integer areaId);
}
