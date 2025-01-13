package org.nio.wallet.transaction;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.config.QueueConfig;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionWorker {
    final SqsClient sqsClient;
    final TransactionService transactionService;
    ScheduledExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(() -> {
            log.info("Worker is running");
            try {
                var getQueueRequest = GetQueueUrlRequest.builder()
                        .queueName(QueueConfig.QUEUE_NAME)
                        .build();
                var queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
                ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                        .waitTimeSeconds(1)
                        .queueUrl("https://sqs.ap-southeast-1.amazonaws.com/730335282524/tranphanthanhlong1812345678")
                        .maxNumberOfMessages(10)
                        .build();
                ReceiveMessageResponse sqsMessages = sqsClient.receiveMessage(receiveMessageRequest);
                if (sqsMessages.hasMessages())
                    sqsMessages.messages().forEach(message -> {
//                        var transferRequest = (TransferRequest) MessageKt.parse(TransferRequest.getDefaultInstance(), message.body());
                        log.debug("Received message: {}", message);
//                        transactionService.persistTransaction(transferRequest);
                    });
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }, 0, 1, SECONDS);
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(10, SECONDS);
    }
//    @Override
//    public void onMessage(Message message) {
//        ObjectMessage m = (ObjectMessage) message;
//        try {
//            TransferRequest request = (TransferRequest) m.getObject();
//            transactionService.persistTransaction(request);
//        } catch (JMSException e) {
//            // TODO: handle fail request
//            e.printStackTrace();
//        }
//    }
}
