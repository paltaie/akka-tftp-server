package com.paltaie.akkatftpserver.model;

import akka.actor.AbstractActor;

public class ErrorActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
