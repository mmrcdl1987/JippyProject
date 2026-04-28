package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmProductVariantRepository extends JpaRepository<FmProductVariant, Integer> {
    List<FmProductVariant> findByProductId(Integer productId);
    void deleteByProductId(Integer productId);
    Optional<FmProductVariant> findByProductIdAndVariantName(Integer productId, String variantName);

    // This fetches variants for multiple products in one DB call
    List<FmProductVariant> findByProductIdIn(List<Integer> productIds);


}
