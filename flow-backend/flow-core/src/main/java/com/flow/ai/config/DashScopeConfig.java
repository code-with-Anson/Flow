package com.flow.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "dashscope")
@Data
public class DashScopeConfig {
    private String apiKey;
    private String model = "qwen2.5-vl-embedding";
    private Integer dimension = 2048;
}
