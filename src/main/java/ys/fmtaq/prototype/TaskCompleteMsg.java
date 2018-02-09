package ys.fmtaq.prototype;

import java.util.Objects;
import java.util.UUID;

public class TaskCompleteMsg {

    private final UUID taskId;
    private final String subQueueId;

    public TaskCompleteMsg(UUID taskId, String subQueueId) {
        this.taskId = taskId;
        this.subQueueId = subQueueId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getSubQueueId() {
        return subQueueId;
    }

    @Override
    public String toString() {
        return "TaskCompleteMsg{taskId='" + taskId + "', subQueueId='" + subQueueId + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TaskCompleteMsg that = (TaskCompleteMsg) o;
        return Objects.equals(getTaskId(), that.getTaskId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId());
    }
}
