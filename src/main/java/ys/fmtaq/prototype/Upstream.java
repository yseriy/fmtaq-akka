package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import ys.fmtaq.prototype.connector.IncomingMsg;

public class Upstream extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(IncomingMsg.class, msg -> System.out.println(msg.getBody())).build();
    }
}
