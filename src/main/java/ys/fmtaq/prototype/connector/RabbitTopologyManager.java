package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.rabbitmq.client.Channel;

import java.io.IOException;

class RabbitTopologyManager extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Channel topologyChannel;
    private final ActorRef publisherRef;
    private final RabbitConnectorConfig.QueueConfig hostQueueConfig;

    public static Props props(final Channel topologyChannel, final Channel outboundChannel,
                              final RabbitConnectorConfig.QueueConfig hostQueueConfig) {
        return Props.create(RabbitTopologyManager.class, () -> new RabbitTopologyManager(topologyChannel,
                outboundChannel, hostQueueConfig));
    }

    private RabbitTopologyManager(final Channel topologyChannel, final Channel outboundChannel,
                                  final RabbitConnectorConfig.QueueConfig hostQueueConfig) {
        this.topologyChannel = topologyChannel;
        this.hostQueueConfig = hostQueueConfig;
        this.publisherRef = getContext().actorOf(RabbitPublisher.props(outboundChannel), "publisher");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OutgoingMsg.class, this::handleOutgoingMsg)
                .build();
    }

    private void handleOutgoingMsg(OutgoingMsg msg) throws IOException {
        topologyChannel.queueDeclare(msg.getAddress(), hostQueueConfig.getDurable(), hostQueueConfig.getExclusive(),
                hostQueueConfig.getAutoDelete(), hostQueueConfig.getArguments());
        publisherRef.tell(msg, getSelf());
    }
}
