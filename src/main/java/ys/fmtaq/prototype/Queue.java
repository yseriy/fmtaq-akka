package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

public class Queue extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(NewTaskMsg.class, this::handleNewTaskMsg)
                .match(TaskCompleteMsg.class, this::handleTaskCompleteMsg)
                .build();
    }

    private void handleNewTaskMsg(final NewTaskMsg msg) {
        ActorRef subQueueRef;
        Option<ActorRef> optionalSubQueueRef = getContext().child(msg.getSubQueueId());

        if (optionalSubQueueRef.isDefined()) {
            subQueueRef = optionalSubQueueRef.get();
        } else {
            subQueueRef = getContext().actorOf(Props.create(SubQueue.class), msg.getSubQueueId());
        }

        subQueueRef.tell(msg, getSelf());
    }

    private void handleTaskCompleteMsg(final TaskCompleteMsg msg) {
        Option<ActorRef> optionalSubQueueRef = getContext().child(msg.getSubQueueId());

        if (optionalSubQueueRef.isEmpty()) {
            log.error("cannot find queue: '{}' to complete task: '{}'", msg.getSubQueueId(), msg.getTaskId());
            return;
        }

        ActorRef subQueueRef = optionalSubQueueRef.get();
        subQueueRef.tell(msg, getSelf());
    }
}
