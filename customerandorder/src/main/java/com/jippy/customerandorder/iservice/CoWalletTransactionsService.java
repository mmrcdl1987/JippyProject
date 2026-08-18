package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;

import java.util.List;

public interface CoWalletTransactionsService {

     List<CoCustomerWalletTransactions> getTransactionsByCustomerId(
            Integer customerId);
     List<CoCustomerWalletTransactions> getAllTransactions();
}
