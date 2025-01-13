package org.nio.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@Configuration
public class QueueConfig {
    public final static String QUEUE_NAME = "tranphanthanhlong1812345678";

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

//    @Bean
//    public SQSConnectionFactory connectionFactory() {
//        return new SQSConnectionFactory(
//                new ProviderConfiguration(),
//                AmazonSQSClientBuilder.defaultClient()
//        );
//    }
//
//    @Bean
//    public Session session(SQSConnectionFactory connectionFactory) throws JMSException {
//        SQSConnection connection = connectionFactory.createConnection();
//        // ready to consume message
//        connection.start();
//
//        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//    }
//
//    @Bean
//    public MessageProducer transactionProducer(Session session) throws JMSException {
//        var queue = session.createQueue("nio-lab");
//        return session.createProducer(queue);
//    }
//
//    @Bean
//    public MessageConsumer transactionConsumer(Session session, TransactionService transactionService) throws JMSException {
//        var queue = session.createQueue("nio-lab");
//        var consumer = session.createConsumer(queue);
//        consumer.setMessageListener(new TransactionWorker(transactionService));
//        return consumer;
//    }
}