package com.jippy.foodandmart.service;


import com.jippy.foodandmart.dto.FmCampaignLocationResponse;

public interface FmCampaignLocationService {

    /**
     * Campaign Location API
     *
     * State Selected
     *      -> Cities + State Outlets
     *
     * State + City Selected
     *      -> Areas + City Outlets
     *
     * State + City + Area Selected
     *      -> Area Outlets
     *
     * @param stateId Required
     * @param cityId Optional
     * @param areaId Optional
     * @return Campaign Location Response
     */
    FmCampaignLocationResponse getCampaignLocation(
            Integer stateId,
            Integer cityId,
            Integer areaId);

}