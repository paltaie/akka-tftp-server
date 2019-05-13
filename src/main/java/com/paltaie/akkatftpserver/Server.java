package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Udp;
import akka.io.UdpMessage;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Server extends AbstractActor {
    private static final String HOST = "0.0.0.0";
    private static final int PORT = 13337;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Map<TftpOpcode, ActorRef> actorMap = new HashMap<>();

    public Server(ActorRef readRequestActor, ActorRef writeRequestActor, ActorRef errorActor, ActorRef ackActor,
                  ActorRef dataActor) {
        actorMap.put(TftpOpcode.READ_REQUEST, readRequestActor);
        actorMap.put(TftpOpcode.WRITE_REQUEST, writeRequestActor);
        actorMap.put(TftpOpcode.ERROR, errorActor);
        actorMap.put(TftpOpcode.ACK, ackActor);
        actorMap.put(TftpOpcode.DATA, dataActor);
        
        final ActorRef mgr = Udp.get(getContext().getSystem()).getManager();
        mgr.tell(
                UdpMessage.bind(getSelf(), new InetSocketAddress(HOST, PORT)),
                getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Bound.class, bound -> {
                    log.info("I'm the cluster singleton! " + bound.toString());
                    getContext().become(ready(getSender()));
                })
                .build();
    }

    private Receive ready(final ActorRef socket) {
        return receiveBuilder()
                .match(Udp.Received.class, r -> {
                    TftpOpcode opcode = TftpOpcode.byOpcode(r.data().toArray()[1]);
                    log.info("Received opcode: {}", opcode);
                    actorMap.get(opcode).tell(r, socket);
                })
                .matchEquals(UdpMessage.unbind(), message -> socket.tell(message, getSelf()))
                .match(Udp.Unbound.class, message -> getContext().stop(getSelf()))
                .build();
    }
}