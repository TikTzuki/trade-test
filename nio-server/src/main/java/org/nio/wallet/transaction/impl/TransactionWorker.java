package org.nio.wallet.transaction.impl;


import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.config.QueueConfig;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionWorker {
    final SqsClient sqsClient;
    final TransactionServiceImpl transactionService;
    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @PostConstruct
    public void init() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(QueueConfig.QUEUE_URL)
                .messageAttributeNames("grpc")
                .waitTimeSeconds(20)
                .build();

        executorService.submit(() -> {
            log.info("Worker is running");
            Callable<Long> initState = () -> 0L;
            BiFunction<Long, SynchronousSink<Long>, Long> infinityMessageGenerator = (i, sink) -> {
                sink.next(i);
                return i;
            };
            Flux.generate(initState, infinityMessageGenerator)
                    .flatMap(i -> {
                        ReceiveMessageResponse sqsMessages = sqsClient.receiveMessage(receiveMessageRequest); // Block generator until has message
                        log.debug("Poll {} messages", sqsMessages.messages().size());
                        return Flux.fromIterable(sqsMessages.messages());
                    })
                    .flatMap(message -> {
                        MessageAttributeValue value = message.messageAttributes().get("grpc");
                        TransferRequest request = null;
                        try {
                            request = TransferRequest.parseFrom(value.binaryValue().asByteArray());
                            log.debug("Received message: {} {}", message, request);
                        } catch (Exception e) {
                            // TODO: handle parse fail request
                            log.error("Failed to parse message: {}", message);
                        }

                        return Objects.nonNull(request)
                                ? transactionService.persistTransaction(request).thenReturn(message)
                                : Mono.empty(); //FIXME: Not delete fail message
                    })
                    .bufferTimeout(10, Duration.ofSeconds(50))
                    .doOnNext(batch -> {
                        var entries = batch.stream().map(message -> DeleteMessageBatchRequestEntry.builder()
                                .id(message.messageId())
                                .receiptHandle(message.receiptHandle())
                                .build()).toList();
                        log.debug("Delete {} messages", entries);
                        sqsClient.deleteMessageBatch(DeleteMessageBatchRequest.builder()
                                .queueUrl(QueueConfig.QUEUE_URL)
                                .entries(entries)
                                .build());
                    })
                    .subscribe(result -> {
                    });
        });
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, MINUTES);
    }

}
