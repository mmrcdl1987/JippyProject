package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.iservice.CoWalletTransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/co/wallet/transactions")

public class CoWalletTransactionsController {

    @Autowired
    private CoWalletTransactionsService walletTransactionsService;

    @GetMapping("/{customerId}")
    public ResponseEntity<List<CoCustomerWalletTransactions>>
    getTransactionsByCustomerId(@PathVariable Integer customerId) {

        return ResponseEntity.ok(
                walletTransactionsService.getTransactionsByCustomerId(customerId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CoCustomerWalletTransactions>>
    getAllTransactions() {

        return ResponseEntity.ok(
                walletTransactionsService.getAllTransactions()
        );
    }
}
