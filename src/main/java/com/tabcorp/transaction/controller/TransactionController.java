package com.tabcorp.transaction.controller;

import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import com.tabcorp.transaction.model.ro.TransactionRO;
import com.tabcorp.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionRO> saveTransaction(@Valid @RequestBody TransactionRO transactionRO) {
        return new ResponseEntity<>(transactionService.saveTransaction(transactionRO), HttpStatus.CREATED);
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<List<TransactionRO>> handleBulkTransactions(@RequestParam("file") MultipartFile file) throws IOException {
        return new ResponseEntity<>(transactionService.saveBulkTransactions(file), HttpStatus.CREATED);
    }

    @GetMapping("/customers-cost")
    public ResponseEntity<List<CustomerTransactionRO>> getTransactionsCostByCustomers(@RequestParam(required = false, name = "customer_id") Long customerId) {
        return new ResponseEntity<>(transactionService.getTransactionsCostByCustomers(customerId), HttpStatus.OK);
    }

    @GetMapping("/products-cost")
    public ResponseEntity<List<ProductTransactionRO>> getTransactionsCostByProducts(@RequestParam(required = false, name = "product_code") String productCode) {
        return new ResponseEntity<>(transactionService.getTransactionsCostByProducts(productCode), HttpStatus.OK);
    }

    @GetMapping("/count/{location}")
    public ResponseEntity<Map<String, Long>> getTransactionCountForLocation(@PathVariable String location) {
        return new ResponseEntity<>(transactionService.getTransactionCountForLocation(location), HttpStatus.OK);
    }
}
