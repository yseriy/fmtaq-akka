package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Task extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String address;
    private final String body;

    public static Props props(final NewTaskMsg newTaskMsg) {
        return Props.create(Task.class, () -> new Task(newTaskMsg));
    }

    private Task(final NewTaskMsg newTaskMsg) {
        super();
        this.address = newTaskMsg.getAddress();
        this.body = newTaskMsg.getBody();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartTaskMsg.class, this::handleStartTaskMsg)
                .match(StopTaskMsg.class, this::handleStopTaskMsg)
                .build();
    }

    private void handleStartTaskMsg(final StartTaskMsg msg) {
        log.info("Started task has address: '{}' and body '{}'", address, body);
    }

    private void handleStopTaskMsg(final StopTaskMsg msg) {
        log.info("task: '{}' complete", getSelf());
        getContext().stop(getSelf());
    }
}
