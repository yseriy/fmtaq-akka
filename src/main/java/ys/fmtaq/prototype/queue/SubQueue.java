package ys.fmtaq.prototype.queue;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;
import java.util.Queue;

class SubQueue extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Queue<TaskMsg> queue = new LinkedList<>();
    private final ActorRef outboundStreamRef;

    public static Props props(final ActorRef outboundStreamRef, final Duration subQueueTimeout) {
        return Props.create(SubQueue.class, () -> new SubQueue(outboundStreamRef, subQueueTimeout));
    }

    private SubQueue(ActorRef outboundStreamRef, Duration subQueueTimeout) {
        this.outboundStreamRef = outboundStreamRef;
        getContext().setReceiveTimeout(subQueueTimeout);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ReceiveTimeout.class, this::handleReceiveTimeout)
                .match(TaskMsg.class, this::handleTaskMsg)
                .match(TaskCompleteMsg.class, this::handleTaskCompleteMsg)
                .build();
    }

    private void handleReceiveTimeout(ReceiveTimeout msg) {
        if (queue.size() == 0) {
            log.info("try to stop idle sub_queue: '{}'", getSelf());
            getContext().stop(getSelf());
        }
    }

    private void handleTaskMsg(final TaskMsg msg) {
        getSender().tell(new AskMsg(), getSelf());
        queue.add(msg);

        if (queue.size() == 1) {
            outboundStreamRef.tell(msg, getSelf());
        }
    }

    private void handleTaskCompleteMsg(final TaskCompleteMsg msg) {
        getSender().tell(new AskMsg(), getSelf());
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

    @Override
    public void postStop() {
        log.info("sub_queue stop: '{}'", getSelf());
    }
}
