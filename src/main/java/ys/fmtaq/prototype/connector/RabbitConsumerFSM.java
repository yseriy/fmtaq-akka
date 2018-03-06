package ys.fmtaq.prototype.connector;

import akka.actor.AbstractFSM;
import akka.actor.Props;
import akka.japi.pf.FSMStateFunctionBuilder;
import akka.japi.pf.FSMTransitionHandlerBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;

enum RabbitConsumerState {
    Run, Stop
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

class RabbitConsumerFSM extends AbstractFSM<RabbitConsumerState, RabbitConsumerData> {

    public static Props props(final Channel inboundChannel, final String queueName) {
        return Props.create(RabbitConsumerFSM.class, () -> new RabbitConsumerFSM(inboundChannel, queueName));
    }

    private RabbitConsumerFSM(final Channel inboundChannel, final String queueName) {
        startWith(RabbitConsumerState.Stop, new RabbitConsumerData(inboundChannel, queueName));

        when(RabbitConsumerState.Run, handleEventsInRunState());
        when(RabbitConsumerState.Stop, handleEventsInStopSate());

        onTransition(handleTransitions());

        initialize();
    }

    private FSMStateFunctionBuilder<RabbitConsumerState, RabbitConsumerData> handleEventsInRunState() {
        return matchEvent(StartConsumeMsg.class, RabbitConsumerData.class, (msg, data) -> stay())
                .event(StopConsumeMsg.class, RabbitConsumerData.class, (msg, data) -> goTo(RabbitConsumerState.Stop));
    }

    private FSMStateFunctionBuilder<RabbitConsumerState, RabbitConsumerData> handleEventsInStopSate() {
        return matchEvent(StartConsumeMsg.class, RabbitConsumerData.class, (msg, data) -> goTo(RabbitConsumerState.Run))
                .event(StopConsumeMsg.class, RabbitConsumerData.class, (msg, data) -> stay());
    }

    private FSMTransitionHandlerBuilder<RabbitConsumerState> handleTransitions() {
        return matchState(RabbitConsumerState.Stop, RabbitConsumerState.Run, this::startConsume)
                .state(RabbitConsumerState.Run, RabbitConsumerState.Stop, this::stopConsume);
    }

    private void startConsume() throws IOException {
        String consumerTag = stateData().getInboundChannel().basicConsume(stateData().getQueueName(), true,
                this::deliverCallback, this::cancelCallback);
        nextStateData().setConsumerTag(consumerTag);
    }

    private void deliverCallback(final String consumerTag, final Delivery message) {
        getContext().getParent().tell(message, getSelf());
    }

    private void cancelCallback(final String consumerTag) {

    }

    private void stopConsume() throws IOException {
        stateData().getInboundChannel().basicCancel(stateData().getConsumerTag());
    }
}
