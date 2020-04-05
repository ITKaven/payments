package com.kaven.payments.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 *    @NotBlank(message = "用户名不能为空")  用于String判断空格
 *    @NotNull 用于引用型
 *    @NotEmpty  用于集合
 * */
@Data
public class UserRegisterForm {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String email;

}
