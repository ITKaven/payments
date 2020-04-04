package com.kaven.payments.enums;

import lombok.Getter;

@Getter
public enum ProductStatusEnum {

    ON_SALE(1) ,

    OFF_SALE(2) ,

    DETELE(3) ,

    ;

    Integer code;
    ProductStatusEnum(Integer code ){
        this.code = code;
    }
}
