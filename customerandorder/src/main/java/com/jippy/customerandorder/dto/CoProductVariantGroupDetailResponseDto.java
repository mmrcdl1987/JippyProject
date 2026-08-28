package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoProductVariantGroupDetailResponseDto {

    private Integer productVariantGroupsId;

    private String groupName;

    private List<CoProductVariantOptionDetailResponseDto> options =
            new ArrayList<>();
}