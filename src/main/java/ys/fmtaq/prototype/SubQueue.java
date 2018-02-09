package ys.fmtaq.prototype;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.Option;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class SubQueue extends AbstractActor {

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
            System.out.println("receive message to create new task: '" + msg.getTaskId()
                    + "' but this task already exists in sub_queue: " + msg.getSubQueueId() + "'");
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
            System.out.println("receive message to complete task: '" + msg.getTaskId()
                    + "' but sub_queue: '" + msg.getSubQueueId() + "' is empty");
            return;
        }

        if (!taskIdForRemove.equals(msg.getTaskId())) {
            System.out.println("receive message to complete task: '" + msg.getTaskId()
                    + "' which is not a head of sub_queue: '" + msg.getSubQueueId() + "'");
            return;
        }

        if (!sendTaskCompleteMsgToTask(msg)) {
            System.out.println("receive message to complete task: '" + msg.getTaskId()
                    + "' but cannot find this task in sub_queue: '" + msg.getSubQueueId() + "'");
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
            System.out.println("cannot find task: '" + taskId + "'");
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
