package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.ReviewRequestDto;
import com.jippy.foodandmart.dto.ReviewResponseDto;

public interface ReviewService {
    ReviewResponseDto saveReview(ReviewRequestDto requestDto);
}
