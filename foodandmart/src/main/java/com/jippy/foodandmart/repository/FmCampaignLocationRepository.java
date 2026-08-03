package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.projections.FmAreaProjection;
import com.jippy.foodandmart.projections.FmCityProjection;
import com.jippy.foodandmart.projections.FmOutletProjection;
import com.jippy.foodandmart.projections.FmOutletsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmCampaignLocationRepository
        extends JpaRepository<FmOutlet, Integer> {

    /**
     * Fetch Cities By State
     */
    @Query(value = """
            SELECT
                    c.city_id AS cityId,
                    c.city_name AS cityName
            FROM jippy_fm.city c
            WHERE c.state_id = :stateId
            ORDER BY c.city_name
            """,
            nativeQuery = true)
    List<FmCityProjection> getCities(
            @Param("stateId") Integer stateId);

    /**
     * Fetch Areas By City
     */
    @Query(value = """
            SELECT
                    a.area_id AS areaId,
                    a.area_name AS areaName
            FROM jippy_fm.area a
            WHERE a.city_id = :cityId
            ORDER BY a.area_name
            """,
            nativeQuery = true)
    List<FmAreaProjection> getAreas(
            @Param("cityId") Integer cityId);

    /**
     * Fetch Campaign Outlets
     */
    @Query(value = """
            SELECT DISTINCT

                    o.outlet_id AS outletId,

                    o.outlet_name AS outletName

            FROM jippy_fm.address ad

            INNER JOIN jippy_fm.outlets o

                    ON ad.jippy_address_id=o.outlet_id

            WHERE ad.address_type='OUTLET'

            AND o.is_approved=true

            AND o.is_active='Y'

            AND(

                    (:areaId IS NOT NULL
                        AND ad.area_id=:areaId)

                OR

                    (:areaId IS NULL
                        AND :cityId IS NOT NULL
                        AND ad.city_id=:cityId)

                OR

                    (:areaId IS NULL
                        AND :cityId IS NULL
                        AND ad.state_id=:stateId)

            )

            ORDER BY o.outlet_name
            """,
            nativeQuery = true)
    List<FmOutletsProjection> getCampaignOutlets(
            @Param("stateId") Integer stateId,
            @Param("cityId") Integer cityId,
            @Param("areaId") Integer areaId);

}