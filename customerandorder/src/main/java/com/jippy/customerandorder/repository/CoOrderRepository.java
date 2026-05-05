package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoOrderRepository extends JpaRepository<CoOrder,String> {

}
