package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.ReviewRequestDto;
import com.jippy.foodandmart.dto.ReviewResponseDto;
import com.jippy.foodandmart.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fm/reviews")
public class ReviewsController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create or Update Review", description = "Create new review or update existing review based on reviewsId")
    public ResponseEntity<FmApiResponse<ReviewResponseDto>> saveReview(@Valid @RequestBody ReviewRequestDto requestDto) {

        log.info("SAVE_REVIEW_API_STARTED | reviewsId={} | reviewId={} | customerId={} | reviewType={}", requestDto.getReviewsId(), requestDto.getReviewId(), requestDto.getCustomerId(), requestDto.getReviewType());

        ReviewResponseDto response = reviewService.saveReview(requestDto);

        log.info("SAVE_REVIEW_API_SUCCESS | reviewsId={} | reviewType={}", response.getReviewsId(), requestDto.getReviewType());

        return ResponseEntity.ok(FmApiResponse.success(FmAppConstants.MSG_REVIEW_SAVED, response));
    }
}