package com.kaven.payments.form;

import lombok.Data;

@Data
public class CartUpdateForm {

    private Integer quantity;

    private Boolean selected;
}
