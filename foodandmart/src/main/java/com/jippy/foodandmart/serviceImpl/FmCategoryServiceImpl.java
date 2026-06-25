package com.jippy.foodandmart.serviceImpl;

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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmCategoryServiceImpl implements IFmCategoryService {

    private final FmCategoryRepository categoryRepository;

    @Override
    @Transactional
    public FmCreateCategoryResponseDto createCategory(
            FmCreateCategoryRequestDto request) {

        log.info("CREATE_CATEGORY_STARTED | categoryName={}",
                request.getCategoryName());

        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName().trim())) {
            throw new DuplicateResourceException(
                    "Category already exists : " + request.getCategoryName());
        }

        FmCategory category = CategoryMapper.toEntity(request, 1);

        FmCategory savedCategory = categoryRepository.save(category);

        log.info("CREATE_CATEGORY_COMPLETED | categoryId={}",
                savedCategory.getCategoryId());

        return CategoryMapper.toResponseDto(savedCategory);
    }
    }