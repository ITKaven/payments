package com.kaven.payments;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@MapperScan(basePackages = "com.kaven.payments.dao")
public class PaymentsApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(PaymentsApplication.class, args);
    }

}
