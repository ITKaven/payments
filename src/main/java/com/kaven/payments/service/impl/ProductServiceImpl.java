package com.kaven.payments.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kaven.payments.dao.ProductMapper;
import com.kaven.payments.pojo.Product;
import com.kaven.payments.service.ICategoryService;
import com.kaven.payments.service.IProductService;
import com.kaven.payments.vo.ProductVo;
import com.kaven.payments.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize) {
        Set<Integer> categoryIdSet = new HashSet<>();
        if(categoryId != null){
            categoryService.findSubCategoryId(categoryId , categoryIdSet);
            categoryIdSet.add(categoryId);
        }

        PageHelper.startPage(pageNum , pageSize);
        List<Product> productList = productMapper.selectByCategoryIdSet(categoryIdSet);
        List<ProductVo> productVoList = productList
                .stream()
                .map(e -> product2ProductVo(e))
                .collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo<>(productList);
        pageInfo.setList(productVoList);
        return ResponseVo.success(pageInfo);
    }

    private ProductVo product2ProductVo(Product product){
        ProductVo productVo = new ProductVo();
        BeanUtils.copyProperties(product , productVo);
        return productVo;
    }
}