package com.tabcorp.transaction.dao;

import com.tabcorp.transaction.model.Transaction;
import com.tabcorp.transaction.model.ro.CustomerTransactionRO;
import com.tabcorp.transaction.model.ro.ProductTransactionRO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("select new com.tabcorp.transaction.model.ro.CustomerTransactionRO(t.customer.customerId, CONCAT(t.customer.firstName, ' ', t.customer.lastName), t.customer.email, t.customer.location, sum(t.quantity * t.product.cost)) from Transaction as t group by t.customer")
    List<CustomerTransactionRO> findTransactionCostPerCustomer();

    @Query("select new com.tabcorp.transaction.model.ro.CustomerTransactionRO(t.customer.customerId, CONCAT(t.customer.firstName, ' ', t.customer.lastName), t.customer.email, t.customer.location, sum(t.quantity * t.product.cost)) from Transaction as t where t.customer.customerId=:customerId")
    List<CustomerTransactionRO> findTransactionCostByCustomer(Long customerId);

    @Query("select new com.tabcorp.transaction.model.ro.ProductTransactionRO(t.product.productCode, t.product.cost, t.product.status, sum(t.quantity * t.product.cost)) from Transaction as t group by t.product")
    List<ProductTransactionRO> findTransactionCostPerProduct();

    @Query("select new com.tabcorp.transaction.model.ro.ProductTransactionRO(t.product.productCode, t.product.cost, t.product.status, sum(t.quantity * t.product.cost)) from Transaction as t where t.product.productCode=:productCode")
    List<ProductTransactionRO> findTransactionCostByProduct(@Param("productCode") String productCode);

    @Query("select count(t.transactionId) from Transaction as t where UPPER(t.customer.location)=:location")
    Long findTransactionCountForLocation(String location);
}
