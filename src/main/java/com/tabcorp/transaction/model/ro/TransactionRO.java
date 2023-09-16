package com.tabcorp.transaction.model.ro;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionRO {

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("customer_id")
    @NotNull(message = "Customer Id is required")
    private Long customerId;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @JsonProperty("transaction_time")
    @NotNull(message = "transaction time is required")
    private LocalDateTime transactionTime;

    @NotNull(message = "Quantity is required")
    private Long quantity;

    @JsonProperty("product_code")
    @NotNull(message = "Product code is required")
    private String productCode;
}
