package com.kaven.payments.dao;

import com.kaven.payments.pojo.PayInfo;
import org.springframework.stereotype.Component;

@Component
public interface PayInfoMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(PayInfo record);

    int insertSelective(PayInfo record);

    PayInfo selectByPrimaryKey(Integer id);

    PayInfo selectByOrderNo(Long orderNo);

    int updateByPrimaryKeySelective(PayInfo record);

    int updateByPrimaryKey(PayInfo record);
}