package ys.fmtaq.prototype;

import akka.actor.ActorSystem;

public class FmtaqAkka {

    public static void main(final String[] args) {

        ActorSystem actorSystem = ActorSystem.create("rabbit_connector");
    }
}
