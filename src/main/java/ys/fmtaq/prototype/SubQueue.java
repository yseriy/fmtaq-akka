package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.LinkedList;
import java.util.Queue;

public class SubQueue extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Queue<TaskMsg> queue = new LinkedList<>();
    private final ActorRef outboundStreamRef;

    public static Props props(final ActorRef outboundStreamRef) {
        return Props.create(SubQueue.class, () -> new SubQueue(outboundStreamRef));
    }

    private SubQueue(ActorRef outboundStreamRef) {
        this.outboundStreamRef = outboundStreamRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskMsg.class, this::handleTaskMsg)
                .match(TaskCompleteMsg.class, this::handleTaskCompleteMsg)
                .build();
    }

    private void handleTaskMsg(final TaskMsg msg) {
        queue.add(msg);

        if (queue.size() == 1) {
            outboundStreamRef.tell(msg, getSelf());
        }
    }

    private void handleTaskCompleteMsg(final TaskCompleteMsg msg) {
        TaskMsg taskMsgForComplete = queue.peek();

        if (taskMsgForComplete == null) {
            log.error("receive message to complete task: '{}' but sub_queue: '{}' is empty",
                    msg.getTaskId(), msg.getSubQueueId());
            return;
        }

        if (!taskMsgForComplete.getTaskId().equals(msg.getTaskId())) {
            log.error("receive message to complete task: '{}' which is not a head of sub_queue: '{}'",
                    msg.getTaskId(), msg.getSubQueueId());
            return;
        }

        queue.poll();
        startNextTask();
    }

    private void startNextTask() {
        TaskMsg msg = queue.peek();

        if (msg != null) {
            outboundStreamRef.tell(msg, getSelf());
        }
    }
}
