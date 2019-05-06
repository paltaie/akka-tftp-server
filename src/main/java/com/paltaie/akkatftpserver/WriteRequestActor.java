package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.cluster.ddata.*;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;
import com.paltaie.akkatftpserver.model.Ack;
import com.paltaie.akkatftpserver.model.WriteRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class WriteRequestActor extends AbstractActor {

    static final Key<LWWMap<InetSocketAddress, WriteRequest>> MAP_KEY = LWWMapKey.create("tftpWriterMap");
    private final Cluster cluster = Cluster.get(context().system());
    private final ActorRef replicator =
            DistributedData.get(getContext().getSystem()).replicator();

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, this::handleReceived)
                .match(Replicator.UpdateSuccess.class, this::handleUpdateSuccess)
                .build();
    }

    private void handleUpdateSuccess(Replicator.UpdateSuccess updateSuccess) {
        log.debug("Update success: {}", updateSuccess);
    }

    private void handleReceived(Udp.Received r) {
        String fileName = TftpUtils.getFileName(r);
        String mode = TftpUtils.getMode(r);
        WriteRequest writeRequest = new WriteRequest(TftpOpcode.WRITE_REQUEST, fileName, mode);

        log.info("Storing requester {} with WRQ {} in CRDT store", r.sender(), writeRequest);
        Replicator.Update<LWWMap<InetSocketAddress, WriteRequest>> update = new Replicator.Update<>(MAP_KEY, LWWMap.create(), Replicator.writeLocal(), map -> map.put(cluster, r.sender(), writeRequest));
        replicator.tell(update, self());
        log.info("Replicator updated.");

        sender().tell(UdpMessage.send(ByteString.fromArray(Ack.initialWRQAck().getBytes()), r.sender()), self());
    }
}
