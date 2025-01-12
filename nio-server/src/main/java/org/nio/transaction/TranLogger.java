package org.nio.transaction;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TranLogger extends Filter<ILoggingEvent> {
    public static final Logger logger = LoggerFactory.getLogger(TranLogger.class);

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        if (Objects.equals(iLoggingEvent.getLoggerName(), logger.getName()))
            return FilterReply.NEUTRAL;
        return FilterReply.DENY;
    }
}