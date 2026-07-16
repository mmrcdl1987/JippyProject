package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.List;

@Data
public class FmProductEditVariantGroupDto {

    private Integer productVariantGroupsId;

    private String groupName;

    private String selectionType;

    private Integer minSelection;

    private Integer maxSelection;

    private Integer displayOrder;

    private List<FmProductEditVariantOptionDto> options;
}