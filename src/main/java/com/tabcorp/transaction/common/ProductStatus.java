package com.tabcorp.transaction.common;

import lombok.Getter;

@Getter
public enum ProductStatus {
    ACTIVE("Active"), INACTIVE("Inactive");

    String label;

    private ProductStatus(String label) {
        this.label = label;
    }
}
