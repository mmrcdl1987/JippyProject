package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FmProductVariantGroupRequestDto {

    /**
     * Null -> Create
     * Not Null -> Update
     */
    private Integer productVariantGroupsId;

    @NotBlank(message = "Group name is required")
    private String groupName;

    @NotBlank(message = "Selection type is required")
    private String selectionType;

    @NotNull(message = "Minimum selection is required")
    private Integer minSelection;

    @NotNull(message = "Maximum selection is required")
    private Integer maxSelection;

    private Integer displayOrder;

    @Valid
    private List<FmProductVariantValueRequestDto> values;
}