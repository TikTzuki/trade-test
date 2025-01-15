package org.nio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "transaction")
public class TransactionConfig {
    Duration receiveMessageWaitTime;
    int bufferSize;
    Duration bufferTime;
}
