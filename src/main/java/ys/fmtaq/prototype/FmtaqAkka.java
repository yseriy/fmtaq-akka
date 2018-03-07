package ys.fmtaq.prototype;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ys.fmtaq.prototype.connector.RabbitConnectorConfig;
import ys.fmtaq.prototype.connector.RabbitConnectorFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class FmtaqAkka {

    public static void main(final String[] args) throws IOException, TimeoutException {
        RabbitConnectorConfig.QueueConfig mainQueueConfig = new RabbitConnectorConfig.QueueConfig("test_main_queue",
                true, false, false, null);
        RabbitConnectorConfig.QueueConfig hostQueueConfig = new RabbitConnectorConfig.QueueConfig(null,
                true, false, false, null);
        RabbitConnectorConfig connectorConfig = new RabbitConnectorConfig("ds", 5672, "devel",
                "devel", mainQueueConfig, hostQueueConfig);

        ActorSystem actorSystem = ActorSystem.create("rabbit_connector");
        ActorRef upstreamRef = actorSystem.actorOf(Props.create(Upstream.class), "upstream");
        RabbitConnectorFactory factory = new RabbitConnectorFactory(actorSystem, upstreamRef, connectorConfig);
        ActorRef connectorRef = factory.createConnector();
    }
}
