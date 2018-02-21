package ys.fmtaq.prototype;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ys.fmtaq.prototype.queue.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FmtaqAkka {

    public static void main(final String[] args) throws IOException {
        FmtaqAkkaConfig config = FmtaqAkkaConfig.INSTANCE;
        InboundQueueOptions options = new InboundQueueOptions(3, 4, 30);

        ActorSystem actorSystem = ActorSystem.create(config.getSystemName());
        ActorRef outboundStream = actorSystem.actorOf(Props.create(OutboundQueue.class), config.getOutboundStreamAddress());
        ActorRef inboundQueue = actorSystem.actorOf(InboundQueue.props(outboundStream, options), config.getInboundStreamAddress());

        List<TaskMsg> messages = new ArrayList<>();
        messages.add(new TaskMsg("sub_queue_1", "address_1", "body_1"));
        messages.add(new TaskMsg("sub_queue_2", "address_2", "body_2"));
        TaskMsg msg3 = new TaskMsg("sub_queue_3", "address_3", "body_3");
        messages.add(msg3);
        TaskMsg msg4 = new TaskMsg("sub_queue_3", "address_3", "body_4");
        messages.add(msg4);

        messages.forEach(msg -> inboundQueue.tell(msg, ActorRef.noSender()));
        inboundQueue.tell(new TaskCompleteMsg(msg3.getTaskId(), msg3.getSubQueueId()), ActorRef.noSender());
        inboundQueue.tell(new TaskCompleteMsg(msg4.getTaskId(), msg4.getSubQueueId()), ActorRef.noSender());

        System.out.println(">>> Press ENTER to exit <<<");

        try {
            int key = System.in.read();
        } finally {
            actorSystem.terminate();
        }
    }
}
