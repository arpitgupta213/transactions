package com.tabcorp.transaction.model.ro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonPropertyOrder({ "customer_id", "customer_name", "email", "location", "total_cost"})
public class CustomerTransactionRO {
    @JsonProperty("customer_id")
    Long customerId;

    @JsonProperty("customer_name")
    String customerName;

    String email;

    String location;

    @JsonProperty("total_transaction_cost")
    double totalCost;
}
