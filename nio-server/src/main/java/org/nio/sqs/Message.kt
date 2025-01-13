package org.nio.sqs

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Message
import mu.KotlinLogging
import org.nio.config.QueueConfig
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

val logger = KotlinLogging.logger {}

fun SqsClient.publish(msg: GeneratedMessageV3) {

    try {
//        val createQueueRequest = CreateQueueRequest.builder()
//            .queueName(QueueConfig.QUEUE_NAME)
//            .build();
//        this.createQueue(createQueueRequest)
        val getQueueRequest = GetQueueUrlRequest.builder()
            .queueName(QueueConfig.QUEUE_NAME)
            .build();

        val queueUrl = this.getQueueUrl(getQueueRequest).queueUrl();
        val sendMsgRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(msg.toByteString().toString())
            .build();

        this.sendMessage(sendMsgRequest);
    } catch (e: Exception) {
        logger.error { e }
    }
}

fun parse(b: GeneratedMessageV3, msg: String): Message {
    val byteString = ByteString.copyFromUtf8(msg);
    return b.toBuilder().mergeFrom(byteString).build()
}
// https://sqs.ap-southeast-1.amazonaws.com/070459239192/nio-lab