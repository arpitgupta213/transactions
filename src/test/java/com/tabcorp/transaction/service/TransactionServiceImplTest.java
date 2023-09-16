package com.tabcorp.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tabcorp.transaction.common.InvalidTransactionException;
import com.tabcorp.transaction.common.ProductStatus;
import com.tabcorp.transaction.dao.CustomerRepository;
import com.tabcorp.transaction.dao.ProductRepository;
import com.tabcorp.transaction.dao.TransactionRepository;
import com.tabcorp.transaction.model.Product;
import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import com.tabcorp.transaction.model.ro.TransactionRO;
import com.tabcorp.transaction.setup.TransactionDataSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TransactionServiceImplTest {

    private static TransactionRepository transactionRepository;
    private static CustomerRepository customerRepository;
    private static ProductRepository productRepository;
    private static TransactionService transactionService;

    @BeforeAll
    public static void setup() {
        transactionRepository = Mockito.mock(TransactionRepository.class);
        customerRepository = Mockito.mock(CustomerRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        transactionService = new TransactionServiceImpl(transactionRepository, productRepository, customerRepository, objectMapper);
    }

    @Test
    public void testSaveTransaction() {
        when(productRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getProduct()));
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        when(transactionRepository.save(any())).thenReturn(TransactionDataSetup.getTransaction());
        TransactionRO transactionRO = transactionService.saveTransaction(TransactionDataSetup.getTransactionRO());
        assertEquals(2L, transactionRO.getQuantity());
        assertEquals("PROD_005", transactionRO.getProductCode());
        assertEquals(1001, transactionRO.getCustomerId());
    }

    @Test
    public void testSaveTransactionWithPastDate() {
        when(productRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getProduct()));
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        TransactionRO transactionRO = TransactionDataSetup.getTransactionRO();
        transactionRO.setTransactionTime(LocalDateTime.now().minusDays(1));
        InvalidTransactionException ex = assertThrows(InvalidTransactionException.class, () -> transactionService.saveTransaction(transactionRO));
        assertEquals("Transaction date is in the past", ex.getMessage());
    }

    @Test
    public void testSaveTransactionWithInvalidProduct() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        InvalidTransactionException ex = assertThrows(InvalidTransactionException.class, () -> transactionService.saveTransaction(TransactionDataSetup.getTransactionRO()));
        assertEquals("Produce code is invalid", ex.getMessage());
    }

    @Test
    public void testSaveTransactionWithInactiveProduct() {
        Product product = TransactionDataSetup.getProduct();
        product.setStatus(ProductStatus.INACTIVE.getLabel());
        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        InvalidTransactionException ex = assertThrows(InvalidTransactionException.class, () -> transactionService.saveTransaction(TransactionDataSetup.getTransactionRO()));
        assertEquals("Product must be active", ex.getMessage());
    }

    @Test
    public void testSaveTransactionWithInvalidTransactionCost() {
        Product product = TransactionDataSetup.getProduct();
        product.setCost(6000);
        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        InvalidTransactionException ex = assertThrows(InvalidTransactionException.class, () -> transactionService.saveTransaction(TransactionDataSetup.getTransactionRO()));
        assertEquals("Transaction cost exceeds 5000", ex.getMessage());
    }

    @Test
    public void testSaveTransactionWithInvalidCustomer() {
        when(productRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getProduct()));
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        InvalidTransactionException ex = assertThrows(InvalidTransactionException.class, () -> transactionService.saveTransaction(TransactionDataSetup.getTransactionRO()));
        assertEquals("Customer is invalid", ex.getMessage());
    }

    @Test
    public void testGetTransactionsCostByCustomers() {
        when(transactionRepository.findTransactionCostPerCustomer()).thenReturn(TransactionDataSetup.getCustomerTransactions());
        List<CustomerTransactionRO> customerTransactions = transactionService.getTransactionsCostByCustomers(null);
        assertTrue(customerTransactions.size() > 0);
        Iterator<CustomerTransactionRO> itr = TransactionDataSetup.getCustomerTransactions().iterator();
        customerTransactions.forEach(t -> {
            CustomerTransactionRO customerTransactionRO = itr.next();
            assertEquals(customerTransactionRO.getCustomerId(), t.getCustomerId());
            assertEquals(customerTransactionRO.getCustomerName(), t.getCustomerName());
            assertEquals(customerTransactionRO.getLocation(), t.getLocation());
            assertEquals(customerTransactionRO.getTotalCost(), t.getTotalCost());
        });
    }

    @Test
    public void testGetTransactionsCostByCustomer() {
        when(transactionRepository.findTransactionCostByCustomer(any())).thenReturn(TransactionDataSetup.getCustomerTransactions());
        List<CustomerTransactionRO> customerTransactions = transactionService.getTransactionsCostByCustomers(10001L);
        assertEquals(1, customerTransactions.size());
        customerTransactions.forEach(t -> {
            assertEquals(10001L, t.getCustomerId());
            assertEquals("Peter Parker", t.getCustomerName());
            assertEquals("US", t.getLocation());
            assertEquals(500, t.getTotalCost());
        });
    }

    @Test
    public void testGetTransactionsCostByProducts() {
        when(transactionRepository.findTransactionCostPerProduct()).thenReturn(TransactionDataSetup.getProductTransactions());
        List<ProductTransactionRO> productTransactions = transactionService.getTransactionsCostByProducts(null);
        assertTrue(productTransactions.size() > 0);
        Iterator<ProductTransactionRO> itr = TransactionDataSetup.getProductTransactions().iterator();
        productTransactions.forEach(t -> {
            ProductTransactionRO productTransactionRO = itr.next();
            assertEquals(productTransactionRO.getProductCode(), t.getProductCode());
            assertEquals(productTransactionRO.getCost(), t.getCost());
            assertEquals(productTransactionRO.getStatus(), t.getStatus());
            assertEquals(productTransactionRO.getTotalCost(), t.getTotalCost());
        });
    }

    @Test
    public void testGetTransactionsCostByProduct() {
        when(transactionRepository.findTransactionCostByProduct(any())).thenReturn(TransactionDataSetup.getProductTransactions());
        List<ProductTransactionRO> productTransactions = transactionService.getTransactionsCostByProducts("PROD_005");
        assertEquals(1, productTransactions.size());
        productTransactions.forEach(t -> {
            assertEquals("PROD_005", t.getProductCode());
            assertEquals(50, t.getCost());
            assertEquals(ProductStatus.ACTIVE.getLabel(), t.getStatus());
            assertEquals(500, t.getTotalCost());
        });
    }

    @Test
    public void testGetTransactionCountForLocation() {
        when(transactionRepository.findTransactionCountForLocation(any())).thenReturn(5L);
        Map<String, Long> transactionCount = transactionService.getTransactionCountForLocation("Australia");
        assertEquals(1, transactionCount.size());
        assertEquals(5L, transactionCount.get("transactionCount"));
    }

    @Test
    public void testSaveBulkTransactions() throws IOException {
        MultipartFile file = new MockMultipartFile("transactions.json", "[{\"customer_id\":10001,\"transaction_time\":\"2023-12-12 10:08:02\",\"quantity\":2,\"product_code\":\"PROD_005\"},{\"customer_id\":10001,\"transaction_time\":\"2023-12-12 10:08:02\",\"quantity\":7,\"product_code\":\"PRODUCT_003\"}]".getBytes(StandardCharsets.UTF_8));
        when(productRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getProduct()));
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        when(transactionRepository.save(any())).thenReturn(TransactionDataSetup.getTransaction());
        List<TransactionRO> transactions = transactionService.saveBulkTransactions(file);
        assertEquals(2, transactions.size());
        transactions.forEach(t -> {
            assertEquals(1001L, t.getCustomerId());
            assertEquals("PROD_005", t.getProductCode());
        });
    }

    @Test
    public void testSaveBulkWithInvalidTransactions() throws IOException {
        MultipartFile file = new MockMultipartFile("transactions.json", "[{\"customer_id\":10001,\"transaction_time\":\"2023-12-12 10:08:02\",\"quantity\":2,\"product_code\":\"PROD_005\"},{\"customer_id\":10001,\"transaction_time\":\"2022-12-12 10:08:02\",\"quantity\":7,\"product_code\":\"PRODUCT_003\"}]".getBytes(StandardCharsets.UTF_8));
        when(productRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getProduct()));
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        when(transactionRepository.save(any())).thenReturn(TransactionDataSetup.getTransaction());
        List<TransactionRO> transactions = transactionService.saveBulkTransactions(file);
        assertEquals(1, transactions.size());
        transactions.forEach(t -> {
            assertEquals(1001L, t.getCustomerId());
            assertEquals("PROD_005", t.getProductCode());
        });
    }

    @Test
    public void testSaveBulkWithAllInvalidTransactions() throws IOException {
        MultipartFile file = new MockMultipartFile("transactions.json", "[{\"customer_id\":10001,\"transaction_time\":\"2022-12-12 10:08:02\",\"quantity\":2,\"product_code\":\"PROD_005\"},{\"customer_id\":10001,\"transaction_time\":\"2022-12-12 10:08:02\",\"quantity\":7,\"product_code\":\"PRODUCT_003\"}]".getBytes(StandardCharsets.UTF_8));
        when(productRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getProduct()));
        when(customerRepository.findById(any())).thenReturn(Optional.of(TransactionDataSetup.getCustomer()));
        when(transactionRepository.save(any())).thenReturn(TransactionDataSetup.getTransaction());
        List<TransactionRO> transactions = transactionService.saveBulkTransactions(file);
        assertEquals(0, transactions.size());
    }
}
