package org.nio.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sqs")
data class QueueConfigProperties(
    val name: String,
    val url: String
)