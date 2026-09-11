package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.ApiResponseDto;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.iservice.CoWalletTransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/co/wallet/transactions")
@RequiredArgsConstructor
public class CoWalletTransactionsController {

    private final CoWalletTransactionsService walletTransactionsService;

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getTransactionsByCustomerId(
            @PathVariable Integer customerId
    ) {

        List<CoCustomerWalletTransactions> transactions =
                walletTransactionsService.getTransactionsByCustomerId(customerId);

        if (transactions.isEmpty()) {
            return ResponseEntity.ok(
                    new ApiResponseDto(
                            true,
                            "No wallet transaction history found"
                    )
            );
        }

        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    public ResponseEntity<?> getAllTransactions() {

        List<CoCustomerWalletTransactions> transactions =
                walletTransactionsService.getAllTransactions();

        if (transactions.isEmpty()) {
            return ResponseEntity.ok(
                    new ApiResponseDto(
                            true,
                            "No wallet transaction history found"
                    )
            );
        }

        return ResponseEntity.ok(transactions);
    }

}
