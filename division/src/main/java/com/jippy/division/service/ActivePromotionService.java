package com.jippy.division.service;

import com.jippy.division.dto.ActivePromotionRequestDto;
import com.jippy.division.dto.ActivePromotionDto;

import java.util.List;

public interface ActivePromotionService {

    List<ActivePromotionDto> getActivePromotions(
            ActivePromotionRequestDto requestDto
    );
}