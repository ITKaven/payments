package com.kaven.payments.service;

import com.kaven.payments.pojo.User;
import com.kaven.payments.vo.ResponseVo;

public interface IUserService {
    /**
     * 注册
     * */
    ResponseVo<User> register(User user);

    /**
     * 登陆
     * */
    ResponseVo<User> login(String username , String password);
}
