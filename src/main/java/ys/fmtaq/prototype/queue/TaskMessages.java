package ys.fmtaq.prototype.queue;

import java.util.UUID;

interface TaskMessages {

    UUID getTaskId();

    String getSubQueueId();
}
