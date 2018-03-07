package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

class RabbitConnector extends AbstractActor {

    private final Channel topologyChannel;
    private final ActorRef upstream;
    private final ActorRef publisherRef;
    private final RabbitConnectorConfig.QueueConfig hostQueueConfig;

    public static Props props(final Channel topologyChannel, final Channel inboundChannel,
                              final Channel outboundChannel, final ActorRef upstream,
                              final RabbitConnectorConfig connectorConfig) {
        return Props.create(RabbitConnector.class, () -> new RabbitConnector(topologyChannel, inboundChannel,
                outboundChannel, upstream, connectorConfig));
    }

    private RabbitConnector(final Channel topologyChannel, final Channel inboundChannel, final Channel outboundChannel,
                            final ActorRef upstream, final RabbitConnectorConfig connectorConfig) {
        this.topologyChannel = topologyChannel;
        this.upstream = upstream;
        this.publisherRef = getContext().actorOf(RabbitPublisher.props(outboundChannel), "publisher");
        this.hostQueueConfig = connectorConfig.getHostQueue();

        String mainQueueName = connectorConfig.getMainQueue().getQueueName();
        Props consumerProps = RabbitConsumer.props(inboundChannel, mainQueueName);
        ActorRef consumerRef = getContext().actorOf(consumerProps, "consumer");
        consumerRef.tell(new StartConsumeMsg(), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Delivery.class, this::handleDelivery)
                .match(OutgoingMsg.class, this::handleOutgoingMsg)
                .build();
    }

    private void handleDelivery(final Delivery delivery) {
        IncomingMsg incomingMsg = new IncomingMsg(new String(delivery.getBody()));
        upstream.tell(incomingMsg, getSelf());
    }

    private void handleOutgoingMsg(final OutgoingMsg msg) throws IOException {
        topologyChannel.queueDeclare(msg.getAddress(), hostQueueConfig.getDurable(), hostQueueConfig.getExclusive(),
                hostQueueConfig.getAutoDelete(), hostQueueConfig.getArguments());
        publisherRef.tell(msg, getSelf());
    }

    @Override
    public void postStop() throws IOException, TimeoutException {
        if (topologyChannel.isOpen()) {
            topologyChannel.close();
        }
    }
}
