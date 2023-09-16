package com.tabcorp.transaction.service;

import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import com.tabcorp.transaction.model.ro.TransactionRO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    TransactionRO saveTransaction(TransactionRO transactionRO);

    List<CustomerTransactionRO> getTransactionsCostByCustomers(Long customerId);

    List<ProductTransactionRO> getTransactionsCostByProducts(String productCode);

    Map<String, Long> getTransactionCountForLocation(String location);

    List<TransactionRO> saveBulkTransactions(MultipartFile file) throws IOException;
}
