package com.kaven.payments.service;

import com.github.pagehelper.PageInfo;
import com.kaven.payments.pojo.Product;
import com.kaven.payments.vo.ProductDetailVo;
import com.kaven.payments.vo.ResponseVo;

import java.util.List;
import java.util.Set;

public interface IProductService {

    ResponseVo<PageInfo> list(Integer categoryId , Integer pageNum , Integer pageSize);

    ResponseVo<ProductDetailVo> detail(Integer productId);

    void insert(Product product);

    List<Product> getProductsByCategoryIdSet(Set<Integer> categoryIdSet);

    void update(Product product);

    void delete(Product product);

    Product getByName(String name);
}
