package com.kaven.payments.controller;

import com.kaven.payments.enums.ResponseEnum;
import com.kaven.payments.form.UserLoginForm;
import com.kaven.payments.form.UserRegisterForm;
import com.kaven.payments.consts.MallConst;
import com.kaven.payments.pojo.User;
import com.kaven.payments.service.IUserService;
import com.kaven.payments.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/user/register")
    public ResponseVo register(@Valid @RequestBody UserRegisterForm userRegisterForm,
                               BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            log.error("注册提交的参数有误，{} {}",
                    bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage());
            return ResponseVo.error(ResponseEnum.PARAM_ERROR , bindingResult);
        }

        User user = new User();
        BeanUtils.copyProperties(userRegisterForm, user);
        log.info("username={}", userRegisterForm.getUsername());
        return userService.register(user);
    }

    @PostMapping("/user/login")
    public ResponseVo<User> login(@Valid @RequestBody UserLoginForm userLoginForm ,
                                  BindingResult bindingResult,
                                  HttpSession session){
        if(bindingResult.hasErrors()){
            log.error("登陆提交的参数有误，{} {}",
                    bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage());
            return ResponseVo.error(ResponseEnum.PARAM_ERROR , bindingResult);
        }
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

    //TODO 判断登录状态， 拦截器
    @PostMapping("/user/logout")
    public ResponseVo logout(HttpSession session){
        session.removeAttribute(MallConst.CURRENT_USER);
        return ResponseVo.success();
    }
}
