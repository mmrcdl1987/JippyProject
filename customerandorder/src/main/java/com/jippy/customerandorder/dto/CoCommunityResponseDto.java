package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoCommunityResponseDto {

    private Integer communityId;
    private String communityName;
    private String communityAreaName;
    private String aboutCommunity;
    private String establishedYear;
    private String communityImageUrl;
    private Integer noOfFamilies;
}
