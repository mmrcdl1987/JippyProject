package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.CustomerResponseDto;
import com.jippy.foodandmart.dto.DriverResponseDto;
import com.jippy.foodandmart.dto.ReviewRequestDto;
import com.jippy.foodandmart.dto.ReviewResponseDto;
import com.jippy.foodandmart.entity.FmReviews;
import com.jippy.foodandmart.feignClients.CustomerAndOrderFeignClient;
import com.jippy.foodandmart.mapper.ReviewsMapper;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.ReviewsRepository;
import com.jippy.foodandmart.service.ReviewService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsServiceImpl implements ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ReviewsMapper reviewsMapper;
    private final FmOutletRepository outletsRepository;
    private final CustomerAndOrderFeignClient customerAndOrderFeignClient;

    @Override
    public ReviewResponseDto saveReview(ReviewRequestDto requestDto) {

        log.info("SAVE_REVIEW_STARTED | reviewsId={} | reviewId={} | customerId={} | reviewType={}", requestDto.getReviewsId(), requestDto.getReviewId(), requestDto.getCustomerId(), requestDto.getReviewType());

        try {

            log.info("VALIDATING_REVIEW_TYPE | reviewType={}", requestDto.getReviewType());

            validateReviewType(requestDto.getReviewType());

            log.info("REVIEW_TYPE_VALIDATION_SUCCESS | reviewType={}", requestDto.getReviewType());

            log.info("VALIDATING_CUSTOMER | customerId={}", requestDto.getCustomerId());

            validateCustomer(requestDto.getCustomerId());

            log.info("CUSTOMER_VALIDATION_SUCCESS | customerId={}", requestDto.getCustomerId());

            log.info("VALIDATING_REVIEW_TARGET | reviewType={} | reviewId={}", requestDto.getReviewType(), requestDto.getReviewId());

            validateReviewTarget(requestDto.getReviewType(), requestDto.getReviewId());

            log.info("REVIEW_TARGET_VALIDATION_SUCCESS | reviewType={} | reviewId={}", requestDto.getReviewType(), requestDto.getReviewId());

            FmReviews reviews;

            if (requestDto.getReviewsId() != null) {

                log.info("UPDATE_REVIEW_FLOW_STARTED | reviewsId={}", requestDto.getReviewsId());

                reviews = reviewsRepository.findById(requestDto.getReviewsId()).orElseThrow(() -> {

                    log.error("REVIEW_NOT_FOUND | reviewsId={}", requestDto.getReviewsId());

                    return new IllegalArgumentException(FmAppConstants.MSG_REVIEW_NOT_FOUND);
                });

                log.info("REVIEW_FETCHED_SUCCESSFULLY | reviewsId={}", reviews.getReviewsId());

                reviewsMapper.mapToEntity(requestDto, reviews);

                reviews.setUpdatedBy(requestDto.getCreatedBy());
                reviews.setUpdatedAt(LocalDateTime.now());

                log.info("REVIEW_ENTITY_MAPPED_FOR_UPDATE | reviewsId={}", reviews.getReviewsId());

            } else {

                log.info("CREATE_REVIEW_FLOW_STARTED | reviewId={} | reviewType={}", requestDto.getReviewId(), requestDto.getReviewType());

                reviews = new FmReviews();

                reviewsMapper.mapToEntity(requestDto, reviews);

                reviews.setCreatedBy(requestDto.getCreatedBy());
                reviews.setCreatedAt(LocalDateTime.now());

                log.info("REVIEW_ENTITY_MAPPED_FOR_CREATE | reviewId={} | reviewType={}", requestDto.getReviewId(), requestDto.getReviewType());
            }

            log.info("DB_SAVE_OPERATION_STARTED | reviewType={} | targetId={}", requestDto.getReviewType(), requestDto.getReviewId());

            FmReviews savedReview = reviewsRepository.save(reviews);

            log.info("DB_SAVE_OPERATION_SUCCESS | reviewsId={}", savedReview.getReviewsId());

            ReviewResponseDto response = reviewsMapper.mapToResponse(savedReview, FmAppConstants.MSG_REVIEW_SAVED);

            log.info("SAVE_REVIEW_SUCCESS | reviewsId={} | customerId={} | reviewType={}", savedReview.getReviewsId(), savedReview.getCustomerId(), savedReview.getReviewType());

            return response;

        } catch (IllegalArgumentException ex) {

            log.error("SAVE_REVIEW_VALIDATION_FAILED | reviewId={} | customerId={} | error={}", requestDto.getReviewId(), requestDto.getCustomerId(), ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("SAVE_REVIEW_FAILED | reviewId={} | customerId={} | error={}", requestDto.getReviewId(), requestDto.getCustomerId(), ex.getMessage(), ex);

            throw ex;
        }
    }

    private void validateCustomer(Integer customerId) {

        try {

            log.info("CUSTOMER_VALIDATION_STARTED | customerId={}", customerId);

            CustomerResponseDto response = customerAndOrderFeignClient.getCustomer(customerId);

            if (response == null || response.getCustomerId() == null) {

                log.error("CUSTOMER_NOT_FOUND | customerId={}", customerId);

                throw new IllegalArgumentException(FmAppConstants.MSG_CUSTOMER_NOT_FOUND);
            }

            log.info("CUSTOMER_VALIDATION_SUCCESS | customerId={}", customerId);

        } catch (FeignException.ServiceUnavailable ex) {

            log.error("CUSTOMER_SERVICE_UNAVAILABLE | customerId={} | error={}", customerId, ex.getMessage());

            throw new IllegalArgumentException("Customer service is currently unavailable. Please try again later.");

        } catch (FeignException ex) {

            log.error("CUSTOMER_VALIDATION_FEIGN_ERROR | customerId={} | error={}", customerId, ex.getMessage());

            throw new IllegalArgumentException(FmAppConstants.MSG_CUSTOMER_NOT_FOUND);

        } catch (Exception ex) {

            log.error("CUSTOMER_VALIDATION_FAILED | customerId={} | error={}", customerId, ex.getMessage(), ex);

            throw new IllegalArgumentException(FmAppConstants.MSG_CUSTOMER_NOT_FOUND);
        }
    }

    private void validateReviewType(String reviewType) {

        log.info("VALIDATE_REVIEW_TYPE_STARTED | reviewType={}", reviewType);

        if (!FmAppConstants.REVIEW_TYPE_OUTLET.equalsIgnoreCase(reviewType) && !FmAppConstants.REVIEW_TYPE_DRIVER.equalsIgnoreCase(reviewType)) {

            log.error("INVALID_REVIEW_TYPE | reviewType={}", reviewType);

            throw new IllegalArgumentException(FmAppConstants.MSG_INVALID_REVIEW_TYPE);
        }

        log.info("VALIDATE_REVIEW_TYPE_SUCCESS | reviewType={}", reviewType);
    }

    private void validateReviewTarget(String reviewType, Integer reviewId) {

        log.info("VALIDATE_REVIEW_TARGET_STARTED | reviewType={} | reviewId={}", reviewType, reviewId);

        if (FmAppConstants.REVIEW_TYPE_OUTLET.equalsIgnoreCase(reviewType)) {

            log.info("VALIDATING_OUTLET | outletId={}", reviewId);

            boolean outletExists = outletsRepository.existsById(reviewId);

            if (!outletExists) {

                log.error("OUTLET_NOT_FOUND | outletId={}", reviewId);

                throw new IllegalArgumentException(FmAppConstants.MSG_OUTLET_NOT_FOUND);
            }

            log.info("OUTLET_VALIDATION_SUCCESS | outletId={}", reviewId);

            return;
        }

        if (FmAppConstants.REVIEW_TYPE_DRIVER.equalsIgnoreCase(reviewType)) {

            try {

                log.info("VALIDATING_DRIVER | driverId={}", reviewId);

                DriverResponseDto response = customerAndOrderFeignClient.getDriverDetails(reviewId);

                if (response == null || response.getDriverId() == null) {

                    log.error("DRIVER_NOT_FOUND | driverId={}", reviewId);

                    throw new IllegalArgumentException(FmAppConstants.MSG_DRIVER_NOT_FOUND);
                }

                log.info("DRIVER_VALIDATION_SUCCESS | driverId={}", reviewId);

            } catch (FeignException.ServiceUnavailable ex) {

                log.error("DRIVER_SERVICE_UNAVAILABLE | driverId={} | error={}", reviewId, ex.getMessage());

                throw new IllegalArgumentException("Driver service is currently unavailable. Please try again later.");

            } catch (FeignException ex) {

                log.error("DRIVER_VALIDATION_FEIGN_ERROR | driverId={} | error={}", reviewId, ex.getMessage());

                throw new IllegalArgumentException(FmAppConstants.MSG_DRIVER_NOT_FOUND);

            } catch (Exception ex) {

                log.error("DRIVER_VALIDATION_FAILED | driverId={} | error={}", reviewId, ex.getMessage(), ex);

                throw new IllegalArgumentException(FmAppConstants.MSG_DRIVER_NOT_FOUND);
            }
        }

        log.info("VALIDATE_REVIEW_TARGET_COMPLETED | reviewType={} | reviewId={}", reviewType, reviewId);
    }
}