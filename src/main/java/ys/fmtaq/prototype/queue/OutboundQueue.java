package ys.fmtaq.prototype.queue;

import akka.actor.AbstractActor;

public class OutboundQueue extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskMsg.class, this::handleTaskMsg).build();
    }

    private void handleTaskMsg(TaskMsg msg) {
        System.out.println(msg);
    }
}
