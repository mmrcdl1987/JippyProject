package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityDto {

    private Integer communityId;

    @NotNull(message = "Community name cannot be null")
    private String communityName;

    @NotNull(message = "Community Area cannot be null")
    private Integer communityAreaId;

    @NotNull(message = "Community Boundary cannot be null")
    private List<List<CoordinateDTO>> boundary;
    private String aboutCommunity;
    private String establishedYear;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer updatedBy;

    @Data
    public static class CoordinateDTO {
        private double longitude;
        private double latitude;
    }
}


