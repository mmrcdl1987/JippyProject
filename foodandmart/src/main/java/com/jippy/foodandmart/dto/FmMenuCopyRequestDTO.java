package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

// ─── Request DTO ──────────────────────────────────────────────────────────────

/**
 * POST /api/menu/copy
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMenuCopyRequestDTO {
    private Integer sourceOutletId;
    private List<Integer> destinationOutletIds;
    private List<Integer> menuItemIds;          // empty / null = copy ALL items
    private CopyOptions options;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CopyOptions {
        @Builder.Default private boolean copyPrices      = true;
        @Builder.Default private boolean copyAvailability = true;
        @Builder.Default private boolean copyImages       = false;
        @Builder.Default private boolean overwriteExisting = false;
    }
}
