package com.tabcorp.transaction.dao;

import com.tabcorp.transaction.model.Product;
import com.tabcorp.transaction.setup.TransactionDataSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    public void setup() {
        productRepository.save(TransactionDataSetup.getProduct());
    }

    @Test
    public void testFindById() {
        Optional<Product> product = productRepository.findById("PROD_005");
        assertTrue(product.isPresent());
    }
}
