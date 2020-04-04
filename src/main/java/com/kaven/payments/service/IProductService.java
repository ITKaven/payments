package com.kaven.payments.service;

import com.github.pagehelper.PageInfo;
import com.kaven.payments.vo.ProductDetailVo;
import com.kaven.payments.vo.ResponseVo;

public interface IProductService {

    ResponseVo<PageInfo> list(Integer categoryId , Integer pageNum , Integer pageSize);

    ResponseVo<ProductDetailVo> detail(Integer productId);
}
