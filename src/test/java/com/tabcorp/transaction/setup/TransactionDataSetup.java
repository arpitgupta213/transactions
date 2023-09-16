package com.tabcorp.transaction.setup;

import com.tabcorp.transaction.common.ProductStatus;
import com.tabcorp.transaction.model.Customer;
import com.tabcorp.transaction.model.Product;
import com.tabcorp.transaction.model.Transaction;
import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import com.tabcorp.transaction.model.ro.TransactionRO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class TransactionDataSetup {

    public static TransactionRO getTransactionRO() {
        TransactionRO transactionRO = new TransactionRO();
        transactionRO.setCustomerId(1001L);
        transactionRO.setQuantity(2L);
        transactionRO.setTransactionTime(LocalDateTime.now().plusDays(1));
        transactionRO.setProductCode("PROD_005");
        return transactionRO;
    }

    public static Product getProduct() {
        Product product = new Product();
        product.setProductCode("PROD_005");
        product.setStatus(ProductStatus.ACTIVE.getLabel());
        product.setCost(50);
        return product;
    }

    public static Customer getCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(1001L);
        customer.setFirstName("Peter");
        customer.setLastName("Parker");
        customer.setEmail("peter.parker@webmail.com");
        customer.setLocation("US");
        return customer;
    }

    public static Transaction getTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(101L);
        transaction.setTransactionDateTime(LocalDateTime.now());
        transaction.setQuantity(2L);
        transaction.setCustomer(getCustomer());
        transaction.setProduct(getProduct());
        return transaction;
    }

    public static List<CustomerTransactionRO> getCustomerTransactions() {
        return Collections.singletonList(new CustomerTransactionRO(10001L, "Peter Parker",
                "peter.parker@webmail.com", "US", 500));
    }

    public static List<ProductTransactionRO> getProductTransactions() {
        return Collections.singletonList(new ProductTransactionRO("PROD_005", 50, ProductStatus.ACTIVE.getLabel(), 500));
    }
}
