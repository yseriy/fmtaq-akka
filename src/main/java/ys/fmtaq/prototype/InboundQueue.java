package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

public class InboundQueue extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef outboundQueueRef;

    public static Props props(final ActorRef outboundQueueRef) {
        return Props.create(InboundQueue.class, () -> new InboundQueue(outboundQueueRef));
    }

    private InboundQueue(final ActorRef outboundQueueRef) {
        this.outboundQueueRef = outboundQueueRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskMsg.class, this::handleTaskMsg)
                .match(TaskCompleteMsg.class, this::handleTaskCompleteMsg)
                .build();
    }

    private void handleTaskMsg(final TaskMsg msg) {
        ActorRef subQueueRef;
        Option<ActorRef> optionalSubQueueRef = getContext().child(msg.getSubQueueId());

        if (optionalSubQueueRef.isDefined()) {
            subQueueRef = optionalSubQueueRef.get();
        } else {
            subQueueRef = getContext().actorOf(SubQueue.props(outboundQueueRef), msg.getSubQueueId());
        }

        subQueueRef.tell(msg, getSelf());
    }

    private void handleTaskCompleteMsg(final TaskCompleteMsg msg) {
        Option<ActorRef> optionalSubQueueRef = getContext().child(msg.getSubQueueId());

        if (optionalSubQueueRef.isEmpty()) {
            log.error("cannot find sub_queue: '{}' to complete task: '{}'",
                    msg.getSubQueueId(), msg.getTaskId());
            return;
        }

        ActorRef subQueueRef = optionalSubQueueRef.get();
        subQueueRef.tell(msg, getSelf());
    }
}
