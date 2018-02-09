package ys.fmtaq.prototype;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FmtaqAkka {

    public static void main(final String[] args) throws IOException {
        ActorSystem actorSystem = ActorSystem.create("Fmtaq");
        ActorRef queueRef = actorSystem.actorOf(Props.create(Queue.class), "queue");

        List<NewTaskMsg> messages = new ArrayList<>();
        messages.add(new NewTaskMsg("sub_queue_1", "address_1", "body_1"));
        messages.add(new NewTaskMsg("sub_queue_2", "address_2", "body_2"));
        messages.add(new NewTaskMsg("sub_queue_3", "address_3", "body_3"));
        messages.add(new NewTaskMsg("sub_queue_3", "address_3", "body_4"));

        messages.forEach(msg -> queueRef.tell(msg, ActorRef.noSender()));

        System.out.println(">>> Press ENTER to exit <<<");

        try {
            int key = System.in.read();
        } finally {
            actorSystem.terminate();
        }
    }
}
