package com.jippy.division.repositary;

import com.jippy.division.entity.DivPriceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DivPriceModelRepository extends JpaRepository<DivPriceModel,Integer>{

}
