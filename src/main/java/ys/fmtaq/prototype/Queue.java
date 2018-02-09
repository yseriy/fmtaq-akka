package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.Option;

public class Queue extends AbstractActor {

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
            System.out.println("cannot find queue: '" + msg.getSubQueueId()
                    + "' to complete task: '" + msg.getTaskId() + "'");
            return;
        }

        ActorRef subQueueRef = optionalSubQueueRef.get();
        subQueueRef.tell(msg, getSelf());
    }
}
