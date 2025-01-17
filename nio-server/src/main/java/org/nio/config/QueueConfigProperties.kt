package org.nio.config

import org.springframework.boot.context.properties.ConfigurationProperties
import software.amazon.awssdk.regions.Region
import java.net.URL

@ConfigurationProperties(prefix = "sqs")
data class QueueConfigProperties(
    val name: String,
    val url: String,
    var region: Region,
    // Use for private connection to queue
    var urlEndpoint: URL? = null
)