package org.nio.sqs

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest
import mu.KotlinLogging
import org.nio.config.QueueConfig
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry

val logger = KotlinLogging.logger {}

fun SqsClient.publish(msgs: List<TransferRequest>) {
    val startBatch = System.currentTimeMillis()
    val entries = msgs.map {
        val metadata = MessageAttributeValue.builder()
            .binaryValue(SdkBytes.fromByteArray(it.toByteArray()))
            .dataType("Binary")
            .build()
        return@map SendMessageBatchRequestEntry.builder()
            .messageAttributes(mapOf("grpc" to metadata))
            .id(it.referenceId)
            .messageBody(it.javaClass.name)
            .build()
    }
    logger.debug(
        "Prepare batch: {} - {} - entries: {}",
        System.currentTimeMillis() - startBatch,
        entries.size,
        entries
    )
    this.sendMessageBatch(
        SendMessageBatchRequest.builder()
            .queueUrl(QueueConfig.QUEUE_URL)
            .entries(entries)
            .build()
    )
    logger.debug(
        "Transfer batch: {} - {}",
        System.currentTimeMillis() - startBatch,
        entries.size
    )
}