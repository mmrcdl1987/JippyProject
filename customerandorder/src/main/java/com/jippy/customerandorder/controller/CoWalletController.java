package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoCustomerWalletResponseDto;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.iservice.CoWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/co/wallet")
@Slf4j
public class CoWalletController {

    @Autowired
    private CoWalletService coWalletService;

    @GetMapping("/{customerId}")
    public ResponseEntity<CoCustomerWalletResponseDto> getByCustomerId(
            @PathVariable Integer customerId){
        return ResponseEntity.ok(
                coWalletService.getByCustomerId(customerId));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CoCustomerWallet> updateByCustomerId(
            @PathVariable Integer customerId,
            @RequestBody CoCustomerWallet walletDetails) {
        return ResponseEntity.ok(
                coWalletService.updateByCustomerId(customerId, walletDetails));
    }

}
