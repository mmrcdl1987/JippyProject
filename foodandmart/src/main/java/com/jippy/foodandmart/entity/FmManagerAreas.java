package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the mapping between
 * a Manager and the Areas assigned to them.
 *
 * One Manager can be assigned to multiple Areas.
 */
@Entity
@Table(name = "manager_areas", schema = "jippy_fm")
@Data
@NoArgsConstructor
public class FmManagerAreas {

    /**
     * Unique identifier for Manager Area Mapping.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manager_areas_id")
    private Integer managerAreasId;

    /**
     * User Id of the Manager.
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * Area Id assigned to the Manager.
     */
    @Column(name = "area_id", nullable = false)
    private Integer areaId;

}