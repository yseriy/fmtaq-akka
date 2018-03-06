package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;

class RabbitConsumer extends AbstractActor {

    private final Channel inboundChannel;
    private final String queueName;
    private String consumerTag;

    public static Props props(Channel inboundChannel, String queueName) {
        return Props.create(RabbitConsumer.class, () -> new RabbitConsumer(inboundChannel, queueName));
    }

    private RabbitConsumer(Channel inboundChannel, String queueName) {
        this.inboundChannel = inboundChannel;
        this.queueName = queueName;
    }

    Channel getInboundChannel() {
        return inboundChannel;
    }

    String getConsumerTag() {
        return consumerTag;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    @Override
    public Receive createReceive() {
        RabbitConsumeStopState rabbitConsumeStopState = new RabbitConsumeStopState(this);
        return rabbitConsumeStopState.createReceive();
    }
}

class RabbitConsumeRunState {

    private final RabbitConsumer consumer;

    RabbitConsumeRunState(RabbitConsumer consumer) {
        this.consumer = consumer;
    }

    AbstractActor.Receive createReceive() {
        return consumer.receiveBuilder()
                .match(StartConsumeMsg.class, this::handleStartConsume)
                .match(StopConsumeMsg.class, this::handleStopConsume)
                .build();
    }

    private void handleStartConsume(StartConsumeMsg msg) {

    }

    private void handleStopConsume(StopConsumeMsg msg) throws IOException {
        consumer.getInboundChannel().basicCancel(consumer.getConsumerTag());
    }
}

class RabbitConsumeStopState {

    private final RabbitConsumer consumer;

    RabbitConsumeStopState(RabbitConsumer consumer) {
        this.consumer = consumer;
    }

    AbstractActor.Receive createReceive() {
        return consumer.receiveBuilder()
                .match(StartConsumeMsg.class, this::handleStartConsume)
                .match(StopConsumeMsg.class, this::handleStopConsume)
                .build();
    }

    private void handleStartConsume(StartConsumeMsg msg) throws IOException {
        RabbitConsumeRunState rabbitConsumeRunState = new RabbitConsumeRunState(consumer);
        String consumerTag = consumer.getInboundChannel().basicConsume(consumer.getQueueName(), true,
                this::deliverCallback, this::cancelCallback);
        consumer.setConsumerTag(consumerTag);
        consumer.getContext().become(rabbitConsumeRunState.createReceive());
    }

    private void deliverCallback(String consumerTag, Delivery message) {
        consumer.getContext().getParent().tell(message, consumer.getSelf());
    }

    private void cancelCallback(String consumerTag) {

    }

    private void handleStopConsume(StopConsumeMsg msg) {
    }
}
