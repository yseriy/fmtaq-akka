package ys.fmtaq.prototype.queue;

import akka.util.Timeout;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class InboundQueueOptions {

    private final Long messageRetry;
    private final Timeout messageRetryTimeout;
    private final Duration subQueueTimeout;

    public InboundQueueOptions(long messageRetry, long messageRetryTimeout, long subQueueTimeout) {
        this.messageRetry = messageRetry;
        this.messageRetryTimeout = new Timeout(messageRetryTimeout, TimeUnit.SECONDS);
        this.subQueueTimeout = Duration.create(subQueueTimeout, TimeUnit.SECONDS);
    }

    public Long getMessageRetry() {
        return messageRetry;
    }

    public Timeout getMessageRetryTimeout() {
        return messageRetryTimeout;
    }

    public Duration getSubQueueTimeout() {
        return subQueueTimeout;
    }
}
