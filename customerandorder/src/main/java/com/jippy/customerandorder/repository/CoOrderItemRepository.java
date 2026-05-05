package com.jippy.customerandorder.repository;
import com.jippy.customerandorder.entity.CoOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoOrderItemRepository extends JpaRepository<CoOrderItem,Long> {

}
