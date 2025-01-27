package org.nio.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.protobuf.InvalidProtocolBufferException;
import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.util.JsonFormat;

import java.util.Objects;

public class DeadLetterLogger extends Filter<ILoggingEvent> {
    public static final Logger deadLetterLogger = LoggerFactory.getLogger("DeadLetterLogger");

    public static void appendDeadLetter(TransferRequest e) {
        try {
            deadLetterLogger.error(JsonFormat.printer().print(e));
        } catch (InvalidProtocolBufferException ex) {
            deadLetterLogger.error(e.toString());
        }
    }

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        if (Objects.equals(iLoggingEvent.getLoggerName(), deadLetterLogger.getName()))
            return FilterReply.NEUTRAL;
        return FilterReply.DENY;
    }

}