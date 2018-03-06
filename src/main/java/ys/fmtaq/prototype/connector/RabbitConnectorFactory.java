package ys.fmtaq.prototype.connector;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitConnectorFactory {

    private final ActorSystem actorSystem;
    private final ActorRef upstream;
    private final RabbitConnectorConfig config;

    public RabbitConnectorFactory(ActorSystem actorSystem, ActorRef upstream, RabbitConnectorConfig config) {
        this.actorSystem = actorSystem;
        this.upstream = upstream;
        this.config = config;
    }

    public ActorRef createConnector() throws IOException, TimeoutException {
        Connection connection = getFactory().newConnection();
        Channel topologyChannel = connection.createChannel();
        Channel inboundChannel = connection.createChannel();
        Channel outboundChannel = connection.createChannel();

        topologyChannel.queueDeclare(config.getMainQueue().getQueueName(), config.getMainQueue().getDurable(),
                config.getMainQueue().getExclusive(), config.getMainQueue().getAutoDelete(),
                config.getMainQueue().getArguments());

        Props connectorProps = RabbitConnector.props(topologyChannel, inboundChannel, outboundChannel, upstream, config);
        return actorSystem.actorOf(connectorProps, "connector");
//        setupConsumer(connection, connectorRef);

//        return connectorRef;
    }

    private ConnectionFactory getFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.getHost());
        factory.setPort(config.getPort());
        factory.setUsername(config.getUsername());
        factory.setPassword(config.getPassword());

        return factory;
    }

//    private void setupConsumer(Connection connection, ActorRef connectorRef) throws IOException {
//        Channel inboundChannel = connection.createChannel();
//        DeliverCallback deliverCallback = (consumerTag, message) -> deliverCallback(connectorRef, consumerTag, message);
//        CancelCallback cancelCallback = this::cancelCallback;
//        inboundChannel.basicConsume(config.getMainQueue().getQueueName(), true, deliverCallback, cancelCallback);
//    }

//    private void deliverCallback(final ActorRef connectorRef, final String consumerTag, final Delivery message) {
//        connectorRef.tell(new IncomingMsg(new String(message.getBody())), ActorRef.noSender());
//    }
//
//    private void cancelCallback(final String consumerTag) {
//
//    }
}
