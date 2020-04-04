package com.kaven.payments.service;

import com.kaven.payments.form.CartAddForm;
import com.kaven.payments.form.CartUpdateForm;
import com.kaven.payments.vo.CartVo;
import com.kaven.payments.vo.ResponseVo;

public interface ICartService {

    ResponseVo<CartVo> add(Integer uid , CartAddForm form);

    ResponseVo<CartVo> list(Integer uid);

    ResponseVo<CartVo> update(Integer uid , Integer productId , CartUpdateForm form);

    ResponseVo<CartVo> delete(Integer uid , Integer productId);

    ResponseVo<CartVo> selectAll(Integer uid);

    ResponseVo<CartVo> unSelectAll(Integer uid);

    ResponseVo<Integer> sum(Integer uid);
}
