package ys.fmtaq.prototype;

import akka.actor.AbstractActor;

public class Task extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(NewTaskMsg.class, this::handleNewTaskMsg).build();
    }

    private void handleNewTaskMsg(final NewTaskMsg msg) {
        System.out.println(msg + " " + getSelf());
    }
}
