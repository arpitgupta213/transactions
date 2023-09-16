package com.tabcorp.transaction.model.ro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonPropertyOrder({ "product_code", "cost", "status", "total_cost"})
public class ProductTransactionRO {

    @JsonProperty("product_code")
    String productCode;

    double cost;

    String status;

    @JsonProperty("total_transaction_cost")
    double totalCost;
}
