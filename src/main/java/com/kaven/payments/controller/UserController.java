package com.kaven.payments.controller;

import com.kaven.payments.consts.MallConst;
import com.kaven.payments.form.UserLoginForm;
import com.kaven.payments.form.UserRegisterForm;
import com.kaven.payments.pojo.User;
import com.kaven.payments.service.IUserService;
import com.kaven.payments.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/user/register")
    public ResponseVo register(@Valid @RequestBody UserRegisterForm userRegisterForm){
        User user = new User();
        BeanUtils.copyProperties(userRegisterForm, user);
        log.info("username={}", userRegisterForm.getUsername());
        return userService.register(user);
    }

    @PostMapping("/user/login")
    public ResponseVo<User> login(@Valid @RequestBody UserLoginForm userLoginForm ,
                                  HttpSession session){
        ResponseVo<User> userResponseVo = userService.login(
                userLoginForm.getUsername() , userLoginForm.getPassword());
        // 设置Session
        session.setAttribute(MallConst.CURRENT_USER, userResponseVo.getData());
        return userResponseVo;
    }

    @GetMapping("/user")
    public ResponseVo<User> userInfo(HttpSession session){
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return ResponseVo.success(user);
    }

    @PostMapping("/user/logout")
    public ResponseVo logout(HttpSession session){
        session.removeAttribute(MallConst.CURRENT_USER);
        return ResponseVo.success();
    }
}
