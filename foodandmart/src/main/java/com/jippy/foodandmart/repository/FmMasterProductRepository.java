package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMasterProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FmMasterProductRepository extends JpaRepository<FmMasterProduct, Integer> {

    // Handled natively by JpaRepository for dynamic pagination
    Page<FmMasterProduct> findAll(Pageable pageable);

    // Required by compareFileWithDB in Service layer
    List<FmMasterProduct> findAllByOrderByMasterProductIdAsc();

    List<FmMasterProduct> findByCategoryIdOrderByMasterProductIdAsc(Integer categoryId);

    boolean existsByMasterProductNameIgnoreCase(String name);

    boolean existsByMasterProductNameIgnoreCaseAndCategoryId(String name, Integer categoryId);

    boolean existsByMasterProductNameIgnoreCaseAndCategoryNameIgnoreCase(
            String masterProductName,
            String categoryName);

    Optional<FmMasterProduct> findByMasterProductNameIgnoreCaseAndCategoryNameIgnoreCase(
            String masterProductName,
            String categoryName);

    @Query("""
            SELECT p FROM FmMasterProduct p WHERE
            LOWER(p.masterProductName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY p.masterProductId ASC
            """)
    List<FmMasterProduct> searchByName(@Param("keyword") String keyword);

    @Query("""
            SELECT p FROM FmMasterProduct p WHERE
            (:type = 'all')
            OR (:type = 'veg'    AND p.veg = 1)
            OR (:type = 'nonveg' AND p.nonVeg = 1)
            ORDER BY p.masterProductId ASC
            """)
    List<FmMasterProduct> filterByType(@Param("type") String type);

    @Query("""
        SELECT p
        FROM FmMasterProduct p
        WHERE p.categoryId = :categoryId
        AND p.publish = 1
        AND (
             :keyword IS NULL
             OR :keyword = ''
             OR LOWER(p.masterProductName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY p.masterProductName
    """)
    List<FmMasterProduct> findProductsByCategoryAndKeyword(
            @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword
    );

    boolean existsByCategoryId(Integer categoryId);

    Optional<FmMasterProduct> findByMasterProductNameIgnoreCaseAndCategoryId(
            String masterProductName,
            Integer categoryId);
}