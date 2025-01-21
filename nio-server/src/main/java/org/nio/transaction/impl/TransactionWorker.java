package org.nio.transaction.impl;


import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.config.QueueConfig;
import org.nio.config.TransactionConfig;
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

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionWorker {
    final SqsClient sqsClient;
    final TransactionServiceImpl transactionService;
    final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    final TransactionConfig transactionConfig;

    @PostConstruct
    public void init() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(QueueConfig.QUEUE_URL)
                .messageAttributeNames("grpc")
                .waitTimeSeconds((int) transactionConfig.getReceiveMessageWaitTime().toSeconds())
                .maxNumberOfMessages(transactionConfig.getNumberOfMessages())
                .build();

        executorService.submit(() -> {
            log.info("Worker started");

            Callable<Long> initState = () -> 0L;
            BiFunction<Long, SynchronousSink<Long>, Long> infinityMessageGenerator = (i, sink) -> {
                sink.next(i);
                return i;
            };
            AtomicLong start = new AtomicLong(System.currentTimeMillis());
            AtomicLong count = new AtomicLong();
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
                    .bufferTimeout(transactionConfig.getBufferSize(), transactionConfig.getBufferTime())
                    .doOnNext(batch -> {
                        var entries = batch.stream()
                                .peek(message -> log.debug("Delete message: {}", message.messageId()))
                                .map(message -> DeleteMessageBatchRequestEntry.builder()
                                        .id(message.messageId())
                                        .receiptHandle(message.receiptHandle())
                                        .build()).toList();
                        sqsClient.deleteMessageBatch(DeleteMessageBatchRequest.builder()
                                .queueUrl(QueueConfig.QUEUE_URL)
                                .entries(entries)
                                .build());
                    })
                    .subscribe(result -> {
                        start.set(System.currentTimeMillis());
                        count.addAndGet(result.size());
                        if (count.get() >= 1_00_000) {
                            var now = System.currentTimeMillis();
                            log.info("Process {} messages in {} ms", count.get(), now - start.get());
                            start.set(now);
                            count.set(0);
                        }
                    });
        });
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(20, SECONDS);
    }

}
