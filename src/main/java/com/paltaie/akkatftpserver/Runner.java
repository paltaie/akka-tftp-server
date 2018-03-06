package com.paltaie.akkatftpserver;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.paltaie.akkatftpserver.model.ErrorActor;

public class Runner {
    public static void main(String[] args) {
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("akka-tftp-server");
        system.actorOf(Props.create(Server.class,
                system.actorOf(Props.create(ReadRequestActor.class),"readRequestActor"),
                system.actorOf(Props.create(WriteRequestActor.class),"writeRequestActor"),
                system.actorOf(Props.create(ErrorActor.class),"errorActor"),
                system.actorOf(Props.create(AckActor.class),"ackActor")), "server");
    }
}
