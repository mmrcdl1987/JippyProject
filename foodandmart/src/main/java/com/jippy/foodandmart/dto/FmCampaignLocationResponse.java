package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.dto.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmCampaignLocationResponse {

    private List<FmStatesDto> states;

    private List<FmCitysDto> cities;

    private List<FmAreasDto> areas;

    private List<FmOutletsDto> outlets;

}