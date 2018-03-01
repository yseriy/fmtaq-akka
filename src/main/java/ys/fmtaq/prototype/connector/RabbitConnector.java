package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitConnector extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ConnectionFactory connectionFactory;
    private Connection connection;

    public RabbitConnector() {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost("ds");
        this.connectionFactory.setPort(5672);
        this.connectionFactory.setUsername("devel");
        this.connectionFactory.setPassword("devel");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RunMsg.class, this::handleRun)
                .match(StopMsg.class, this::handleStop)
                .build();
    }

    private void handleRun(final RunMsg msg) throws IOException, TimeoutException {
        if (connection != null && connection.isOpen()) {
            log.error("rabbit connect already open. ignore run command");
            return;
        }

        connection = connectionFactory.newConnection();
    }

    private void handleStop(StopMsg msg) throws IOException {
        closeConnection();
    }

    @Override
    public void postRestart(Throwable reason) throws IOException {
        closeConnection();
    }

    @Override
    public void postStop() throws IOException {
        closeConnection();
    }

    private void closeConnection() throws IOException {
        if (connection.isOpen()) {
            connection.close();
        }
    }
}
