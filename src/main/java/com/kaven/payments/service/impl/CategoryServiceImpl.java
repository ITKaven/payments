package com.kaven.payments.service.impl;

import com.kaven.payments.consts.MallConst;
import com.kaven.payments.dao.CategoryMapper;
import com.kaven.payments.pojo.Category;
import com.kaven.payments.service.ICategoryService;
import com.kaven.payments.vo.CategoryVo;
import com.kaven.payments.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ResponseVo<List<CategoryVo>> selectAll() {
        List<Category> categories = categoryMapper.selectAll();

        List<CategoryVo> categoryVoList = categories.stream()
                .filter(e -> e.getParentId().equals(MallConst.ROOT_PARENT_ID))
                .map(this::category2CategoryVo)
                .sorted(Comparator.comparing(CategoryVo::getSortOrder).reversed())
                .collect(Collectors.toList());

        findSubCategory(categoryVoList , categories);
        return ResponseVo.success(categoryVoList);
    }

    @Override
    public void findSubCategoryId(Integer id, Set<Integer> resultSet) {
        List<Category> categories = categoryMapper.selectAll();
        findSubCategoryId(id , resultSet , categories);
    }

    private void findSubCategoryId(Integer id , Set<Integer> resultSet ,
                                   List<Category> categories){
        for (Category category : categories){
            if(id.equals(category.getParentId())){
                resultSet.add(category.getId());
                findSubCategoryId(category.getId() , resultSet , categories);
            }
        }
    }

    private void findSubCategory(List<CategoryVo> categoryVoList , List<Category> categories){
        for (CategoryVo categoryVo : categoryVoList){
            List<CategoryVo> subCategoryVoList = new ArrayList<>();
            for (Category category : categories){
                if(categoryVo.getId().equals(category.getParentId())){
                    CategoryVo subCategoryVo = category2CategoryVo(category);
                    subCategoryVoList.add(subCategoryVo);
                }
            }
            subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
            categoryVo.setSubCategories(subCategoryVoList);
            if(subCategoryVoList.size() != 0 ){
                findSubCategory(subCategoryVoList , categories);
            }
        }
    }

    private CategoryVo category2CategoryVo(Category category){
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category , categoryVo);
        return categoryVo;
    }
}
