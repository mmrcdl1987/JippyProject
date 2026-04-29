package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.service.COOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class COorderController {

    public  static Logger logger = LoggerFactory.getLogger(COorderController.class);

    @Autowired
    COOrderService COOrderService;

    @GetMapping("/order")
     public String order(){
        logger.info("order api is called");
       // COOrderService.placeOrder();
        return "order api is called";
     }
}
