package com.jippy.customerandorder.projection;

import jakarta.persistence.criteria.CriteriaBuilder;

public interface CoActiveCommunityGroupOrdersProjection {

    Integer getGroupOrdersInvitationId();
    Integer getOrderClosingTimeInMinutes();
    Integer getMaxMembers();
    Integer getCommunityId();
    Integer getActiveOrdersCount();
}
