package ys.fmtaq.prototype.connector;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.rabbitmq.client.Channel;

import java.io.IOException;

class RabbitPublisher extends AbstractActor {

    private final Channel outboundChannel;

    public static Props props(final Channel outboundChannel) {
        return Props.create(RabbitPublisher.class, () -> new RabbitPublisher(outboundChannel));
    }

    private RabbitPublisher(final Channel outboundChannel) {
        this.outboundChannel = outboundChannel;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(OutgoingMsg.class, this::handleOutgoingMsg).build();
    }

    private void handleOutgoingMsg(final OutgoingMsg msg) throws IOException {
        outboundChannel.basicPublish("", msg.getAddress(), null, msg.getBody().getBytes());
    }
}
