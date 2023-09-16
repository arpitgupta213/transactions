package com.tabcorp.transaction.dao;

import com.tabcorp.transaction.model.Transaction;
import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import com.tabcorp.transaction.setup.TransactionDataSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ProductRepository productRepository;

    private Transaction transaction = new Transaction();

    @BeforeEach
    public void setup() {
        productRepository.save(TransactionDataSetup.getProduct());
        customerRepository.save(TransactionDataSetup.getCustomer());
        transaction.setQuantity(2L);
        transaction.setTransactionDateTime(LocalDateTime.now().plusDays(1));
        transaction.setProduct(productRepository.findAll().get(0));
        transaction.setCustomer(customerRepository.findAll().get(0));
    }

    @AfterEach
    public void cleanup() {
        productRepository.deleteAll();
        customerRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    public void testSaveTransaction() {
        transaction = transactionRepository.save(transaction);
        assertNotNull(transaction.getTransactionId());
        assertEquals(1, transactionRepository.findAll().size());
    }

    @Test
    public void testFindTransactionCostPerCustomer() {
        transactionRepository.save(transaction);
        List<CustomerTransactionRO> customerTransactions = transactionRepository.findTransactionCostPerCustomer();
        customerTransactions.forEach(t -> {
            assertEquals(100, t.getTotalCost());
            assertEquals("Peter Parker", t.getCustomerName());
        });
    }

    @Test
    public void testFindTransactionCostByCustomer() {
        transactionRepository.save(transaction);
        List<CustomerTransactionRO> customerTransactions = transactionRepository.findTransactionCostByCustomer(1L);
        customerTransactions.stream().forEach(t -> {
            assertEquals(100, t.getTotalCost());
            assertEquals("Peter Parker", t.getCustomerName());
        });
    }

    @Test
    public void testFindTransactionCostPerProduct() {
        transactionRepository.save(transaction);
        List<ProductTransactionRO> productTransactions = transactionRepository.findTransactionCostPerProduct();
        productTransactions.stream().forEach(t -> {
            assertEquals(100, t.getTotalCost());
            assertEquals("PROD_005", t.getProductCode());
        });
    }

    @Test
    public void testFindTransactionCostByProduct() {
        transactionRepository.save(transaction);
        List<ProductTransactionRO> productTransactions = transactionRepository.findTransactionCostByProduct("PROD_005");
        productTransactions.stream().forEach(t -> {
            assertEquals(100, t.getTotalCost());
            assertEquals("PROD_005", t.getProductCode());
        });
    }

    @Test
    public void testFindTransactionCountForLocation() {
        transactionRepository.save(transaction);
        Long transactionCount = transactionRepository.findTransactionCountForLocation("US");
        assertEquals(1L, transactionCount);
    }
}
