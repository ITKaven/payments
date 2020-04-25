package com.kaven.payments.controller;

import com.kaven.payments.pojo.PayInfo;
import com.kaven.payments.service.impl.PayServiceImpl;
import com.kaven.payments.vo.PayInfoVo;
import com.kaven.payments.vo.ResponseVo;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/pay")
@Slf4j
public class PayController {

    @Autowired
    private PayServiceImpl payServiceImpl;

    @Autowired
    private WxPayConfig wxPayConfig;

    @GetMapping("/create")
    @ResponseBody
    public ResponseVo<PayInfoVo> create(@RequestParam("orderId") String orderId,
                                        @RequestParam("amount") BigDecimal amount,
                                        @RequestParam("payType") BestPayTypeEnum bestPayTypeEnum){
        PayResponse payResponse = payServiceImpl.create(orderId, amount, bestPayTypeEnum);

        //支付方式不同，渲染方式也不同
        //微信native支付用codeurl
        //支付宝pc支付用body
        if(bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE){
//            map.put("codeUrl",payResponse.getCodeUrl());
//            map.put("orderId",orderId);
//            map.put("returnUrl",wxPayConfig.getReturnUrl());
            PayInfoVo payInfoVo = new PayInfoVo();
            payInfoVo.setContent(payResponse.getCodeUrl());
            return ResponseVo.success(payInfoVo);
        }
        else if(bestPayTypeEnum == BestPayTypeEnum.ALIPAY_PC){
//            map.put("body",payResponse.getBody());
            PayInfoVo payInfoVo = new PayInfoVo();
            payInfoVo.setContent(payResponse.getBody());
            return ResponseVo.success(payInfoVo);
        }
        throw  new RuntimeException("暂不支持的支付类型！");
    }

    @PostMapping("notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData){
        return payServiceImpl.asyncNotify(notifyData);
    }

    @GetMapping("queryByOrderId")
    @ResponseBody
    public PayInfo queryByOrderId(@RequestParam("orderId") String orderId){
        log.info("查询接口正在被调用！");
        return payServiceImpl.queryByOrderId(orderId);
    }
}
