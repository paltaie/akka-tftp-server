package com.paltaie.akkatftpserver;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import com.paltaie.akkatftpserver.model.ErrorActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Runner {
    public static void main(String[] args) {
        // boot up server using the route as defined below
        Config config = ConfigFactory.load("application");
        ActorSystem system = ActorSystem.create(config.getString("actor-system-name"));
        ClusterSingletonManagerSettings clusterSingletonManagerSettings = ClusterSingletonManagerSettings.create(system);
        system.actorOf(
                ClusterSingletonManager.props(
                        Props.create(Server.class,
                            system.actorOf(Props.create(ReadRequestActor.class),"readRequestActor"),
                            system.actorOf(Props.create(WriteRequestActor.class),"writeRequestActor"),
                            system.actorOf(Props.create(ErrorActor.class),"errorActor"),
                            system.actorOf(Props.create(AckActor.class),"ackActor")), PoisonPill.getInstance(),
                        clusterSingletonManagerSettings
                )
        );
//        system.actorOf(Props.create(Server.class,
//                system.actorOf(Props.create(ReadRequestActor.class),"readRequestActor"),
//                system.actorOf(Props.create(WriteRequestActor.class),"writeRequestActor"),
//                system.actorOf(Props.create(ErrorActor.class),"errorActor"),
//                system.actorOf(Props.create(AckActor.class),"ackActor")), "server");
    }
}
