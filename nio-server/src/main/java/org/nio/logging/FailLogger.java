package org.nio.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.nio.wallet.grpc.WalletServiceOuterClass;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FailLogger extends Filter<ILoggingEvent> {
    public static final Logger failLogger = LoggerFactory.getLogger("FailLogger");

    public static void appendFail(WalletServiceOuterClass.TransferRequest transaction, @Nullable Throwable cause) {
        try {
            failLogger.error(JsonFormat.printer().print(transaction), cause);
        } catch (InvalidProtocolBufferException ex) {
            failLogger.error(transaction.toString(), cause);
        }
    }
    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        if (Objects.equals(iLoggingEvent.getLoggerName(), failLogger.getName()))
            return FilterReply.NEUTRAL;
        return FilterReply.DENY;
    }
}
