package ys.fmtaq.prototype;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubQueueTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testSendMessage() {
//        TestKit testKit = new TestKit(system);
//        ActorRef actorRef = system.actorOf(Props.create(Queue.class), "test-main");
//
//        actorRef.tell("pprint", ActorRef.noSender());
//        String message = testKit.expectMsgClass(String.class);
//        assertEquals("test_print", message);
    }
}
