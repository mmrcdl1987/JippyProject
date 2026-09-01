package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderCheckoutTax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoOrderCheckoutTaxRepository
        extends JpaRepository<CoOrderCheckoutTax, Integer> {
}