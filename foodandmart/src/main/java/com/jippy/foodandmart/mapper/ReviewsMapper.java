package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.ReviewRequestDto;
import com.jippy.foodandmart.dto.ReviewResponseDto;
import com.jippy.foodandmart.entity.FmReviews;
import org.springframework.stereotype.Component;

@Component
public class ReviewsMapper {

    public void mapToEntity(ReviewRequestDto dto, FmReviews entity) {

        entity.setReviewId(dto.getReviewId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setRating(dto.getRating());
        entity.setReviewText(dto.getReviewText());
        entity.setReviewType(dto.getReviewType());
    }

    public ReviewResponseDto mapToResponse(FmReviews reviews, String message) {

        ReviewResponseDto response = new ReviewResponseDto();
        response.setReviewsId(reviews.getReviewsId());
        response.setMessage(message);

        return response;
    }
}