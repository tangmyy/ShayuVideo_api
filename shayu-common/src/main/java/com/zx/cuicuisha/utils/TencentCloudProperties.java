package com.zx.cuicuisha.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource("classpath:tencentcloud.properties")
@ConfigurationProperties(prefix = "tencent.cloud")
public class TencentCloudProperties {
   private String secretId;
   private String secretKey;
}
