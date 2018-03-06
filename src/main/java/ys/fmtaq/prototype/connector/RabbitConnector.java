package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitConnector extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Channel topologyChannel;
    private final ActorRef downstream;
    private final ActorRef publisherRef;
    private final RabbitConnectorConfig.QueueConfig hostQueueConfig;

    public static Props props(final Channel topologyChannel, final Channel inboundChannel,
                              final Channel outboundChannel, final ActorRef downstream,
                              final RabbitConnectorConfig connectorConfig) {
        return Props.create(RabbitConnector.class, () -> new RabbitConnector(topologyChannel, inboundChannel,
                outboundChannel, downstream, connectorConfig));
    }

    private RabbitConnector(final Channel topologyChannel, final Channel inboundChannel, final Channel outboundChannel,
                            final ActorRef downstream, final RabbitConnectorConfig connectorConfig) {
        this.topologyChannel = topologyChannel;
        this.downstream = downstream;
        this.publisherRef = getContext().actorOf(RabbitPublisher.props(outboundChannel), "publisher");
        this.hostQueueConfig = connectorConfig.getHostQueue();

        String mainQueueName = connectorConfig.getMainQueue().getQueueName();
        ActorRef consumerRef = getContext().actorOf(RabbitConsumerFSM.props(inboundChannel, mainQueueName));
        consumerRef.tell(StartConsumeMsg.class, getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(IncomingMsg.class, this::handleIncomingMsg)
                .match(OutgoingMsg.class, this::handleOutgoingMsg)
                .build();
    }

    private void handleIncomingMsg(final IncomingMsg msg) {
        downstream.tell(msg, getSelf());
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
