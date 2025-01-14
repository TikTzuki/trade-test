package org.nio

import org.nio.config.QueueConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(QueueConfigProperties::class)
class Main

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}
