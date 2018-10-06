package com.paltaie.akkatftpserver;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import com.paltaie.akkatftpserver.model.ErrorActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {
    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
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
                ), "serverSingleton");
        LOG.info("Welcome to akka-tftp-server " + Runner.class.getPackage().getImplementationVersion() + "!");
    }
}
