package com.kaven.payments.service;

import com.github.pagehelper.PageInfo;
import com.kaven.payments.vo.OrderVo;
import com.kaven.payments.vo.ResponseVo;

public interface IOrderService {

    ResponseVo<OrderVo> create(Integer uid , Integer shippingId);

    ResponseVo<PageInfo> list(Integer uid , Integer pageNum , Integer pageSize);

    ResponseVo<OrderVo> detail(Integer uid , Long orderNo);

    ResponseVo cancel(Integer uid , Long orderNo);
}
