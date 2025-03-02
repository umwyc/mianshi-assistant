package com.wyc.mianshi_assistant.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyuncs.exceptions.ClientException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(value = "alioss")
@Configuration
public class AliOssConfig {

    /**
     * oss endpoint 示例：https://oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * oss bucketName
     */
    private String bucketName;

    /**
     * oss region 示例：cn-hangzhou
     */
    private String region;


    @Bean
    public OSS ossClient() {
        try {
            EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            return OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .region(region)
                    .build();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }
}
