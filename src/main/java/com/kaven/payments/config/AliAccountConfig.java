package com.kaven.payments.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ali")
@Data
public class AliAccountConfig {

    private String appId;

    private String privateKey;

    private String aliPayPublicKey;

    private String notifyUrl;

    private String returnUrl;
}
