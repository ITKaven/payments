package com.kaven.payments.service;

import com.kaven.payments.pojo.Category;
import com.kaven.payments.vo.CategoryVo;
import com.kaven.payments.vo.ResponseVo;

import java.util.List;
import java.util.Set;

public interface ICategoryService {

    ResponseVo<List<CategoryVo>> selectAll();

    void findSubCategoryId(Integer id , Set<Integer> resultSet);

    void insert(Category category);

    Integer selectByName(String name);
}
