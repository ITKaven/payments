package com.kaven.payments.pojo;

import lombok.Data;

@Data
public class CartProduct {
    private Integer productId;

    private Integer quantity;

    private Boolean productSelected;

    public CartProduct(){}

    public CartProduct(Integer productId, Integer quantity, Boolean productSelected) {
        this.productId = productId;
        this.quantity = quantity;
        this.productSelected = productSelected;
    }
}
