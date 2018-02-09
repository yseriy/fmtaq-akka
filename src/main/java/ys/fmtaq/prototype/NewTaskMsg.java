package ys.fmtaq.prototype;

import java.util.Objects;
import java.util.UUID;

public class NewTaskMsg {
    private final UUID taskId;
    private final String subQueueId;
    private final String address;
    private final String body;

    NewTaskMsg(final String subQueueId, final String address, final String body) {
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
        return "NewTaskMsg{taskId='" + taskId + "', subQueueId='" + subQueueId
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

        NewTaskMsg newTaskMsg = (NewTaskMsg) o;
        return Objects.equals(getTaskId(), newTaskMsg.getTaskId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId());
    }
}
