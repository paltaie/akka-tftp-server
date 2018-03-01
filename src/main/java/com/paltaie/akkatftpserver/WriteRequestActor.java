package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;

public class WriteRequestActor extends AbstractActor {
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder().build();
    }
}
