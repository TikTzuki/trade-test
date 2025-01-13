package org.nio.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class QueueConfig {
    public final static String QUEUE_NAME = "nio-lab";
    public static String QUEUE_URL;

    @Bean
    public SqsClient sqsClient() {
        var client = SqsClient.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
        QUEUE_URL = client.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(QueueConfig.QUEUE_NAME)
                .build()).queueUrl();
        return client;
    }

//    @Bean
//    public SQSConnectionFactory connectionFactory() {
//        return new SQSConnectionFactory(
//                new ProviderConfiguration(),
//                AmazonSQSClientBuilder.standard()
//                        .withRegion(AP_SOUTHEAST_1.getName())
//        );
//    }
//
//    @Bean
//    public Connection connection(SQSConnectionFactory connectionFactory) throws JMSException {
//        return connectionFactory.createConnection();
//    }
//
//    @Bean
//    public Session session(Connection connection) throws JMSException {
//        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//    }
//
//    @Bean
//    public MessageProducer transactionProducer(Session session) throws JMSException {
//        var queue = session.createQueue(QUEUE_NAME);
//        return session.createProducer(queue);
//    }


}