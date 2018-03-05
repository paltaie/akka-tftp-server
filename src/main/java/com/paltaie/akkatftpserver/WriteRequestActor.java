package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;
import com.paltaie.akkatftpserver.model.Error;

public class WriteRequestActor extends AbstractActor {
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, this::handleReceived)
                .build();
    }

    private void handleReceived(Udp.Received r) {
        Error error = new Error(1, "Sorry, this operation is not yet supported");
        sender().tell(UdpMessage.send(ByteString.fromArray(error.getBytes()), r.sender()), self());
        context().stop(self());
    }
}
