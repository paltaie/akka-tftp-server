package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.io.Udp;

public class AckActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, a -> System.out.println("Ack! " + a))
                .build();
    }
}
