package com.jippy.foodandmart.projections;

// Projection interface for fetching outlet details in settlement process
//used in CoFmOutletDto to fetch outlet details from FM microservice
// and use it in order creation and settlement process in Customer and Order microservice

public interface FmOutletSettlementProjection {

    Integer getOutletId();

    String getOutletName();

    String getOutletPhone();

    Integer getAreaId();

    String getAreaName();

}

