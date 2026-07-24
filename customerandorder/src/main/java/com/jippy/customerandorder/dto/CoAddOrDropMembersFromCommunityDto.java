package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoAddOrDropMembersFromCommunityDto {

    private  Integer customerId;
    private Integer communityId;
    private String type;

}
