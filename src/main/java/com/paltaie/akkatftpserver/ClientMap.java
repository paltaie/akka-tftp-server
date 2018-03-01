package com.paltaie.akkatftpserver;

import akka.actor.ActorRef;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class ClientMap {
    public static ConcurrentHashMap<InetSocketAddress, ActorRef> map = new ConcurrentHashMap<>();
}
