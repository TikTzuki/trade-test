package org.nio.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class QueueConfig implements InitializingBean {
    public static String QUEUE_URL = "";
    final QueueConfigProperties properties;

    @Override
    public void afterPropertiesSet() throws Exception {
        QUEUE_URL = getQueueUrl();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    public String getQueueName() {
        return properties.getName();
    }

    public String getQueueUrl() {
        return properties.getUrl();
    }

}