package com.tabcorp.transaction.dao;

import com.tabcorp.transaction.model.Customer;
import com.tabcorp.transaction.setup.TransactionDataSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    Long customerId;

    @BeforeEach
    public void setup() {
        Customer customer = customerRepository.save(TransactionDataSetup.getCustomer());
        customerId = customer.getCustomerId();
    }

    @Test
    public void testFindById() {
        Optional<Customer> customer = customerRepository.findById(customerId);
        assertTrue(customer.isPresent());
    }
}
