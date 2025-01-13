package org.nio.sqs

import com.google.protobuf.GeneratedMessageV3
import mu.KotlinLogging
import org.nio.config.QueueConfig
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

val logger = KotlinLogging.logger {}

fun SqsClient.publish(msg: GeneratedMessageV3) {

    try {
        val metadata = MessageAttributeValue.builder()
            .binaryValue(SdkBytes.fromByteArray(msg.toByteArray()))
            .dataType("Binary")
            .build()
        val sendMsgRequest = SendMessageRequest.builder()
            .queueUrl(QueueConfig.QUEUE_URL)
            .messageAttributes(mapOf("grpc" to metadata))
            .messageBody(msg.javaClass.name)
            .build()

        this.sendMessage(sendMsgRequest)
    } catch (e: Exception) {
        logger.error { e }
    }
}