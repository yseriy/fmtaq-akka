package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Task extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(NewTaskMsg.class, this::handleNewTaskMsg).build();
    }

    private void handleNewTaskMsg(final NewTaskMsg msg) {
        log.info("{} {}", msg, getSelf());
    }
}
