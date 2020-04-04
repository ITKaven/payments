package com.kaven.payments.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterForm {

    @NotBlank //(message = "用户名不能为空")  //用于 String 判断空格
//    @NotNull
//    @NotEmpty  用于集合
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String email;

}
