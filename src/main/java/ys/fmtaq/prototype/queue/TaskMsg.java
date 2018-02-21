package ys.fmtaq.prototype.queue;

import java.util.Objects;
import java.util.UUID;

public class TaskMsg implements TaskMessages {
    private final UUID taskId;
    private final String subQueueId;
    private final String address;
    private final String body;

    public TaskMsg(final String subQueueId, final String address, final String body) {
        this.taskId = UUID.randomUUID();
        this.subQueueId = subQueueId;
        this.address = address;
        this.body = body;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getSubQueueId() {
        return subQueueId;
    }

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "TaskMsg{taskId='" + taskId + "', subQueueId='" + subQueueId
                + "', address='" + address + "', body='" + body + "'}";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TaskMsg taskMsg = (TaskMsg) o;
        return Objects.equals(getTaskId(), taskMsg.getTaskId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId());
    }
}
