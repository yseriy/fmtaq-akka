package ys.fmtaq.prototype.queue;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.PatternsCS;
import scala.Option;

import java.util.concurrent.ExecutionException;

public class InboundQueue extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef outboundQueueRef;
    private final InboundQueueOptions inboundQueueOptions;

    public static Props props(final ActorRef outboundQueueRef, final InboundQueueOptions inboundQueueOptions) {
        return Props.create(InboundQueue.class, () -> new InboundQueue(outboundQueueRef, inboundQueueOptions));
    }

    private InboundQueue(final ActorRef outboundQueueRef, final InboundQueueOptions inboundQueueOptions) {
        this.outboundQueueRef = outboundQueueRef;
        this.inboundQueueOptions = inboundQueueOptions;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskMsg.class, this::handleTaskMsg)
                .match(TaskCompleteMsg.class, this::handleTaskCompleteMsg)
                .build();
    }

    private void handleTaskMsg(final TaskMsg msg) {
        if (messageNotSend(msg)) {
            log.error("cannot send message: '{}' to sub_queue: '{}'. drop message", msg, msg.getSubQueueId());
        }
    }

    private void handleTaskCompleteMsg(final TaskCompleteMsg msg) {
        Option<ActorRef> optionalSubQueueRef = getContext().child(msg.getSubQueueId());

        if (optionalSubQueueRef.isEmpty()) {
            log.error("cannot find sub_queue: '{}' to complete task: '{}'",
                    msg.getSubQueueId(), msg.getTaskId());
            return;
        }

        if (messageNotSend(msg)) {
            log.error("cannot send message: '{}' to sub_queue: '{}'. drop message", msg, msg.getSubQueueId());
        }
    }

    private boolean messageNotSend(final TaskMessages msg) {
        return !sendMessage(msg);
    }

    private boolean sendMessage(final TaskMessages msg) {
        boolean sendOut = false;

        for (int i = 0; i < inboundQueueOptions.getMessageRetry(); i++) {
            sendOut = tryAsk(getSubQueue(msg.getSubQueueId()), msg);

            if (sendOut) {
                break;
            }
        }

        return sendOut;
    }

    private ActorRef getSubQueue(final String subQueueId) {
        Option<ActorRef> optionActor = getContext().child(subQueueId);

        if (optionActor.isDefined()) {
            return optionActor.get();
        } else {
            return getContext().actorOf(SubQueue.props(outboundQueueRef, inboundQueueOptions.getSubQueueTimeout()),
                    subQueueId);
        }
    }

    private boolean tryAsk(final ActorRef actor, final TaskMessages msg) {
        try {
            PatternsCS.ask(actor, msg, inboundQueueOptions.getMessageRetryTimeout()).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }

        return true;
    }
}
