package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;

class RabbitConsumer extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final AbstractActor.Receive runStage;
    private final Channel channel;
    private final String queueName;

    public static Props props(final Channel channel, final String queueName) {
        return Props.create(RabbitConsumer.class, () -> new RabbitConsumer(channel, queueName));
    }

    private RabbitConsumer(final Channel channel, final String queueName) {
        this.channel = channel;
        this.queueName = queueName;
        this.runStage = receiveBuilder().match(RunMsg.class, this::handleRunMsgInRunStage).build();
    }

    private void handleRunMsgInRunStage(final RunMsg msg) {
        log.error("consume receive run message in run stage. ignoring message");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RunMsg.class, this::handleRunMsg).build();
    }

    private void handleRunMsg(final RunMsg msg) throws IOException {
        channel.basicConsume(queueName, true, this::deliverCallback, this::cancelCallback);
        getContext().become(runStage);
    }

    private void deliverCallback(final String consumerTag, final Delivery message) {
        getContext().getParent().tell(new IncomingMsg(new String(message.getBody())), getSelf());
    }

    private void cancelCallback(final String consumerTag) {
        log.error("consumer: '{}' canceled by server", consumerTag);
    }
}
