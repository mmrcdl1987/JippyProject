package com.jippy.foodandmart.projections;

/**
 * ===========================================================
 * Projection for Driver Address Details
 * ===========================================================
 *
 * Used to fetch Driver Address information from the FM
 * Address table along with State, City and Area names.
 */
public interface FmDriverAddressProjection {

    /*==========================================================
     = Address Details
     ==========================================================*/

    Integer getAddressId();

    String getBuildingNumber();

    String getRoad();

    String getLandmark();

    /*==========================================================
     = Location Ids
     ==========================================================*/

    Integer getStateId();

    Integer getCityId();
    Integer getAreaId();

    /*==========================================================
     = Location Names
     ==========================================================*/

    String getStateName();

    String getCityName();

    String getAreaName();
}