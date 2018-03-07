package com.paltaie.akkatftpserver;

import akka.actor.ActorRef;
import com.paltaie.akkatftpserver.model.ReadRequest;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientMap {
    public static Map<InetSocketAddress, ActorRef> map = new ConcurrentHashMap<>();
    public static Map<InetSocketAddress, ReadRequest> map2 = new ConcurrentHashMap<>();
}
