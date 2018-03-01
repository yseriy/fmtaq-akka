package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

class RabbitTopologyManager extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final RabbitConfig.QueueConfig mainQueueConfig;
    private final RabbitConfig.QueueConfig hostQueueConfig;
    private final AbstractActor.Receive runStage;
    private final AbstractActor.Receive stopStage;
    private final Connection connection;
    private Channel topologyChannel;
    private ActorRef publisherRef;

    public static Props props(final Connection connection, final RabbitConfig.QueueConfig mainQueueConfig,
                              final RabbitConfig.QueueConfig hostQueueConfig) {
        return Props.create(RabbitTopologyManager.class,
                () -> new RabbitTopologyManager(connection, mainQueueConfig, hostQueueConfig));
    }

    private RabbitTopologyManager(final Connection connection, final RabbitConfig.QueueConfig mainQueueConfig,
                                  final RabbitConfig.QueueConfig hostQueueConfig) {
        this.connection = connection;
        this.mainQueueConfig = mainQueueConfig;
        this.hostQueueConfig = hostQueueConfig;

        this.runStage = receiveBuilder()
                .match(RunMsg.class, this::handleRunMsgInRunStage)
                .match(IncomingMsg.class, this::handleIncomingMsgInRunStage)
                .match(OutgoingMsg.class, this::handleOutgoingMsgInRunStage)
                .build();
        this.stopStage = receiveBuilder()
                .match(RunMsg.class, this::handleRunMsgInStopStage)
                .match(IncomingMsg.class, this::handleIncomingMsgInStopStage)
                .match(OutgoingMsg.class, this::handleOutgoingMsgInStopStage)
                .build();
    }

    private void handleRunMsgInRunStage(final RunMsg msg) {
        log.error("topology manager receive run message in run stage. ignoring message.");
    }

    private void handleIncomingMsgInRunStage(final IncomingMsg msg) {
        getContext().getParent().tell(msg, getSelf());
    }

    private void handleOutgoingMsgInRunStage(final OutgoingMsg msg) throws IOException {
        topologyChannel.queueDeclare(msg.getAddress(), hostQueueConfig.getDurable(), hostQueueConfig.getExclusive(),
                hostQueueConfig.getAutoDelete(), hostQueueConfig.getArguments());
        publisherRef.tell(msg, getSelf());
    }

    private void handleRunMsgInStopStage(final RunMsg msg) throws IOException {
        queueDeclare();
        startConsumer();
        publisherRef = startPublisher();
        getContext().become(runStage);
    }

    private void queueDeclare() throws IOException {
        topologyChannel = connection.createChannel();
        topologyChannel.queueDeclare(mainQueueConfig.getQueueName(), mainQueueConfig.getDurable(),
                mainQueueConfig.getExclusive(), mainQueueConfig.getAutoDelete(), mainQueueConfig.getArguments());
    }

    private void startConsumer() throws IOException {
        Props consumerProps = RabbitConsumer.props(connection.createChannel(), mainQueueConfig.getQueueName());
        ActorRef consumerRef = getContext().actorOf(consumerProps, "consumer");
        consumerRef.tell(new RunMsg(), getSelf());
    }

    private ActorRef startPublisher() throws IOException {
        Props publisherProps = RabbitPublisher.props(connection.createChannel());
        return getContext().actorOf(publisherProps, "publisher");
    }

    private void handleIncomingMsgInStopStage(final IncomingMsg msg) {
        log.error("topology manager receive incoming message: '{}' in stop stage. drop message", msg.getBody());
    }

    private void handleOutgoingMsgInStopStage(final OutgoingMsg msg) {
        log.error("topology manager receive outgoing message: '{}' in stop stage. drop message", msg.getBody());
    }

    @Override
    public Receive createReceive() {
        return stopStage;
    }
}
