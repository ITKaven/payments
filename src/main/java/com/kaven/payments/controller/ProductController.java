package com.kaven.payments.controller;

import com.github.pagehelper.PageInfo;
import com.kaven.payments.collectinfo.Clear;
import com.kaven.payments.collectinfo.JingDong;
import com.kaven.payments.service.ICategoryService;
import com.kaven.payments.service.IProductService;
import com.kaven.payments.vo.ProductDetailVo;
import com.kaven.payments.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class ProductController {


    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IProductService productService;

    @GetMapping("/products")
    public ResponseVo<PageInfo> list(@RequestParam(required = false) Integer categoryId ,
                                     @RequestParam(required = false , defaultValue = "1") Integer pageNum ,
                                     @RequestParam(required = false , defaultValue = "10") Integer pageSize){
        return productService.list(categoryId , pageNum , pageSize);
    }

    @GetMapping("/products/{productId}")
    public ResponseVo<ProductDetailVo> detail(@PathVariable Integer productId){
        return productService.detail(productId);
    }

    @PutMapping("/products/add")
    public void add() throws IOException, InterruptedException {
        JingDong jingDong = new JingDong();
        jingDong.collect(categoryService , productService);
    }

    @PutMapping("/products/clear")
    public void clear(){
        Clear clear = new Clear();
        clear.clear(categoryService , productService);
    }
}
