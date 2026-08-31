package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmManagerAreas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for Manager Area Mapping.
 *
 * Provides database operations related to
 * assigning Managers to Areas.
 */
@Repository
public interface FmManagerAreasRepository
        extends JpaRepository<FmManagerAreas, Integer> {

    /**
     * Checks whether the specified Area
     * is already assigned to the given Manager.
     *
     * @param userId Manager User Id.
     * @param areaId Area Id.
     * @return true if mapping already exists, otherwise false.
     */
    boolean existsByUserIdAndAreaId(Integer userId, Integer areaId);

//    /**
//     * Fetches all Area mappings
//     * for the specified Manager.
//     *
//     * @param userId Manager User Id.
//     * @return List of Manager Area mappings.
//     */
//    List<FmManagerAreas> findByUserId(Integer userId);

    /**
     * Checks whether the Manager
     * has at least one Area assigned.
     *
     * @param userId Manager User Id.
     * @return true if mappings exist.
     */
    boolean existsByUserId(Integer userId);

    /**
     * Deletes all Area mappings
     * for the specified Manager.
     *
     * This method will be useful
     * when implementing Update Manager Areas API.
     *
     * @param userId Manager User Id.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        DELETE FROM FmManagerAreas m
        WHERE m.userId = :userId
    """)
    void deleteByUserId(Integer userId);

//  ----------------------------------------------------------------------------------------------------
    /**
     * Fetch all areas assigned
     * to a manager.
     *
     * Example
     *
     * userId =14
     *
     * Returns
     * Area ID's
     * Area 1
     * Area 2
     * Area 3
     */
    List<FmManagerAreas> findByUserId(Integer userId);


    /**
     * Updates the Manager Id for all mapped Areas.
     *
     * Business Rule:
     * When an Approval Setting Approver changes,
     * all Area mappings of the previous Approver
     * are transferred to the new Approver.
     *
     * Example:
     *
     * Before
     * -------
     * User 80
     * Area 1
     * Area 2
     * Area 3
     *
     * After
     * ------
     * User 81
     * Area 1
     * Area 2
     * Area 3
     */
            @Modifying
            @Transactional
            @Query("""
        UPDATE FmManagerAreas m
        SET m.userId=:newUserId
        WHERE m.userId=:oldUserId
        """)
    int updateManagerAreas(
            @Param("oldUserId") Integer oldUserId,
            @Param("newUserId") Integer newUserId);
}
