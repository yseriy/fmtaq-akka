package ys.fmtaq.prototype.connector;

import akka.actor.AbstractFSM;
import akka.actor.FSM;
import akka.actor.Props;
import akka.japi.pf.FSMStateFunctionBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;

enum RabbitConsumerState {
    Run, Idle
}

final class RabbitConsumerData {
    private final Channel inboundChannel;
    private final String queueName;
    private String consumerTag;

    RabbitConsumerData(final Channel inboundChannel, final String queueName) {
        this.inboundChannel = inboundChannel;
        this.queueName = queueName;
    }

    public Channel getInboundChannel() {
        return inboundChannel;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }
}

class RabbitConsumer extends AbstractFSM<RabbitConsumerState, RabbitConsumerData> {

    public static Props props(final Channel inboundChannel, final String queueName) {
        return Props.create(RabbitConsumer.class, () -> new RabbitConsumer(inboundChannel, queueName));
    }

    private RabbitConsumer(final Channel inboundChannel, final String queueName) {
        startWith(RabbitConsumerState.Idle, new RabbitConsumerData(inboundChannel, queueName));

        when(RabbitConsumerState.Run, handleRunState());
        when(RabbitConsumerState.Idle, handleIdleSate());

        initialize();
    }

    private FSMStateFunctionBuilder<RabbitConsumerState, RabbitConsumerData> handleRunState() {
        return matchEvent(StopConsumeMsg.class, this::stopConsume);
    }

    private FSM.State<RabbitConsumerState, RabbitConsumerData> stopConsume(
            final StopConsumeMsg msg, final RabbitConsumerData data) throws IOException {

        String consumerTag = data.getConsumerTag();
        Channel channel = data.getInboundChannel();

        if (consumerTag != null) {
            channel.basicCancel(data.getConsumerTag());
            data.setConsumerTag(null);
        }

        return goTo(RabbitConsumerState.Idle);
    }

    private FSMStateFunctionBuilder<RabbitConsumerState, RabbitConsumerData> handleIdleSate() {
        return matchEvent(StartConsumeMsg.class, this::startConsume);
    }

    private FSM.State<RabbitConsumerState, RabbitConsumerData> startConsume(
            final StartConsumeMsg msg, final RabbitConsumerData data) throws IOException {

        final boolean autoAck = true;
        String queueName = data.getQueueName();
        Channel channel = data.getInboundChannel();

        String consumerTag = channel.basicConsume(queueName, autoAck, this::deliverCallback, this::cancelCallback);
        data.setConsumerTag(consumerTag);

        return goTo(RabbitConsumerState.Run);
    }

    private void deliverCallback(final String consumerTag, final Delivery message) {
        getContext().getParent().tell(message, getSelf());
    }

    private void cancelCallback(final String consumerTag) {
        log().error("server canceled consume. consumer: '{}'. go to idle state", consumerTag);
        getSelf().tell(new StopConsumeMsg(), getSelf());
    }
}
