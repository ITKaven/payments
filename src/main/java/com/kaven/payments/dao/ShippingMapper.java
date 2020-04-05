package com.kaven.payments.dao;

import com.kaven.payments.pojo.Shipping;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByIdAndUid(@Param("uid") Integer uid ,
                         @Param("shippingId") Integer shippingId);

    List<Shipping> selectByUid(Integer uid);
}