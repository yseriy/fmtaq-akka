package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class SubQueue extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Queue<UUID> queue = new LinkedList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(NewTaskMsg.class, this::handleNewTaskMsg)
                .match(TaskCompleteMsg.class, this::handleTaskCompleteMsg)
                .build();
    }

    private void handleNewTaskMsg(final NewTaskMsg msg) {
        Option<ActorRef> optionalTaskRef = getContext().child(msg.getTaskId().toString());

        if (optionalTaskRef.isDefined()) {
            log.error("receive message to create new task: '{}' but this task already exists in sub_queue: '{}'",
                    msg.getTaskId(), msg.getSubQueueId());
            return;
        }

        ActorRef taskRef = getContext().actorOf(Props.create(Task.class), msg.getTaskId().toString());
        taskRef.tell(msg, getSelf());

        queue.add(msg.getTaskId());
        startTaskIfItIsQueueHead(taskRef);
    }

    private void startTaskIfItIsQueueHead(final ActorRef taskRef) {
        if (queue.size() == 1) {
            taskRef.tell(new StartTaskMsg(), getSelf());
        }
    }

    private void handleTaskCompleteMsg(final TaskCompleteMsg msg) {
        UUID taskIdForRemove = queue.peek();

        if (taskIdForRemove == null) {
            log.error("receive message to complete task: '{}' but sub_queue: '{}' is empty", msg.getTaskId(),
                    msg.getSubQueueId());
            return;
        }

        if (!taskIdForRemove.equals(msg.getTaskId())) {
            log.error("receive message to complete task: '{}' which is not a head of sub_queue: '{}'",
                    msg.getTaskId(), msg.getSubQueueId());
            return;
        }

        if (!sendTaskCompleteMsgToTask(msg)) {
            log.error("receive message to complete task: '{}' but cannot find this task in sub_queue: '{}'",
                    msg.getTaskId(), msg.getSubQueueId());
            return;
        }

        queue.poll();
        startNextTask();
    }

    private boolean sendTaskCompleteMsgToTask(final TaskCompleteMsg msg) {
        Option<ActorRef> optionalTaskRef = getContext().child(msg.getTaskId().toString());

        if (optionalTaskRef.isEmpty()) {
            return false;
        }

        ActorRef taskRef = optionalTaskRef.get();
        taskRef.tell(msg, getSelf());

        return true;
    }

    private void startNextTask() {
        UUID taskId = queue.peek();

        if (taskId == null) {
            return;
        }

        if (!sendStartTaskMsgToTask(taskId)) {
            log.error("cannot find task: '{}' to start in sub_queue: '{}'", taskId, getSelf().path().name());
        }
    }

    private boolean sendStartTaskMsgToTask(final UUID taskId) {
        Option<ActorRef> optionalTaskRef = getContext().child(taskId.toString());

        if (optionalTaskRef.isEmpty()) {
            return false;
        }

        ActorRef taskRef = optionalTaskRef.get();
        taskRef.tell(new StartTaskMsg(), getSelf());

        return true;
    }
}
