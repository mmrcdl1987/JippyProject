package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmCreateCategoryRequestDto;
import com.jippy.foodandmart.dto.FmCreateCategoryResponseDto;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.mapper.CategoryMapper;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.service.IFmCategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmCategoryServiceImpl implements IFmCategoryService {

    private final FmCategoryRepository categoryRepository;

    @Override
    @Transactional
    public FmCreateCategoryResponseDto createCategory(FmCreateCategoryRequestDto request) {

        log.info("CREATE_CATEGORY_STARTED | categoryName={}", request.getCategoryName());

        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName().trim())) {
            throw new DuplicateResourceException(
                    "Category already exists : " + request.getCategoryName());
        }

        FmCategory category = CategoryMapper.toEntity(request);

        FmCategory savedCategory = categoryRepository.save(category);

        log.info("CREATE_CATEGORY_COMPLETED | categoryId={}",
                savedCategory.getCategoryId());

        return CategoryMapper.toResponseDto(savedCategory);
    }

    //    ----------------------------------------------------------------------------
    @Override
    public List<FmCreateCategoryResponseDto> getHomeOrAllCategories(String filter) {

        log.info("GET_HOME_OR_ALL_CATEGORIES_STARTED | filter={}", filter);

        List<FmCategory> categoryList;

        if (FmAppConstants.CATEGORY_TYPE_ALL.equalsIgnoreCase(filter)) {

            log.info("Fetching all categories");

            categoryList = categoryRepository.findAll();

        } else if (FmAppConstants.CATEGORY_TYPE_HOME.equalsIgnoreCase(filter)) {

            log.info("Fetching HOME categories");

            categoryList = categoryRepository.findByCategoryType(FmAppConstants.CATEGORY_TYPE_HOME);

        } else {

            throw new IllegalArgumentException(
                    "Invalid filter. Allowed values are ALL or HOME.");
        }

        List<FmCreateCategoryResponseDto> responseList = new ArrayList<>();

        for (FmCategory category : categoryList) {
            responseList.add(CategoryMapper.toResponseDto(category));
        }

        log.info("GET_HOME_OR_ALL_CATEGORIES_COMPLETED | totalCategories={}",
                responseList.size());

        return responseList;
    }

    }