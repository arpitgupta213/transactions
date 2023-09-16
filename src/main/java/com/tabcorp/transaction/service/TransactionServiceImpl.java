package com.tabcorp.transaction.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tabcorp.transaction.common.InvalidTransactionException;
import com.tabcorp.transaction.common.ProductStatus;
import com.tabcorp.transaction.dao.CustomerRepository;
import com.tabcorp.transaction.dao.ProductRepository;
import com.tabcorp.transaction.dao.TransactionRepository;
import com.tabcorp.transaction.model.Customer;
import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.Product;
import com.tabcorp.transaction.model.Transaction;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import com.tabcorp.transaction.model.ro.TransactionRO;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public TransactionRO saveTransaction(TransactionRO transactionRO) {
        Optional<Product> product = productRepository.findById(transactionRO.getProductCode());
        Optional<Customer> customer = customerRepository.findById(transactionRO.getCustomerId());
        validateFields(transactionRO, product, customer);
        Transaction transaction = transactionRepository.save(convertToEntity(transactionRO, product.get(), customer.get()));
        return convertToRO(transaction);
    }

    @Override
    public List<CustomerTransactionRO> getTransactionsCostByCustomers(Long customerId) {
        return Objects.isNull(customerId) ? transactionRepository.findTransactionCostPerCustomer()
                : transactionRepository.findTransactionCostByCustomer(customerId);
    }

    @Override
    public List<ProductTransactionRO> getTransactionsCostByProducts(String productCode) {
        return Strings.isBlank(productCode) ? transactionRepository.findTransactionCostPerProduct()
                : transactionRepository.findTransactionCostByProduct(productCode);
    }

    @Override
    public Map<String, Long> getTransactionCountForLocation(String location) {
        Map<String, Long> transactionCount = new HashMap<>(1);
        transactionCount.put("transactionCount", transactionRepository.findTransactionCountForLocation(location.toUpperCase()));
        return transactionCount;
    }

    @Override
    public List<TransactionRO> saveBulkTransactions(MultipartFile file) throws IOException {
        List<TransactionRO> transactions = objectMapper.readValue(file.getInputStream(), new TypeReference<List<TransactionRO>>() {});
        return transactions.parallelStream().map(this::saveTransactionSafely).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /*
    * This method saves bulk transactions safely. If any transaction is invalid,
    * it logs the error and continues saving other transactions
    * */
    private TransactionRO saveTransactionSafely(TransactionRO transactionRO) {
        try {
            return saveTransaction(transactionRO);
        } catch(Exception e) {
            //Log and ignore invalid transaction. If required, failed transactions can be
            //saved in a file and can be later used for retry
            log.error("Error saving transaction. {}", e.getMessage());
            return null;
        }
    }

    private void validateFields(TransactionRO transactionRO, Optional<Product> product, Optional<Customer> customer) {
        if(transactionRO.getTransactionTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTransactionException("Transaction date is in the past");
        }
        if(product.isEmpty()) {
            throw new InvalidTransactionException("Produce code is invalid");
        }
        if(product.get().getCost() * transactionRO.getQuantity() > 5000) {
            throw new InvalidTransactionException("Transaction cost exceeds 5000");
        }
        if(!product.get().getStatus().equals(ProductStatus.ACTIVE.getLabel())) {
            throw new InvalidTransactionException("Product must be active");
        }
        if(customer.isEmpty()) {
            throw new InvalidTransactionException("Customer is invalid");
        }
    }

    private TransactionRO convertToRO(Transaction transaction) {
        TransactionRO transactionRO = new TransactionRO();
        transactionRO.setTransactionId(transaction.getTransactionId());
        transactionRO.setTransactionTime(transaction.getTransactionDateTime());
        transactionRO.setQuantity(transaction.getQuantity());
        transactionRO.setCustomerId(transaction.getCustomer().getCustomerId());
        transactionRO.setProductCode(transaction.getProduct().getProductCode());
        return transactionRO;
    }

    private Transaction convertToEntity(TransactionRO transactionRO, Product product, Customer customer) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionRO.getTransactionId());
        transaction.setTransactionDateTime(transactionRO.getTransactionTime());
        transaction.setQuantity(transactionRO.getQuantity());
        transaction.setCustomer(customer);
        transaction.setProduct(product);
        return transaction;
    }
}
