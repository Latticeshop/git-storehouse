package com.cbg.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun")
public class AliyunKeyConfig {
    private String SMS_ACCESS_KEY_ID;
    private String SECRET;
}
