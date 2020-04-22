package com.kaven.payments.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class Shipping {
    private Integer id;

    private Integer userId;

    private String receiverName;

    private String receiverMobile;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverAddress;

    private String receiverZip;

    private Date createTime;

    private Date updateTime;
}