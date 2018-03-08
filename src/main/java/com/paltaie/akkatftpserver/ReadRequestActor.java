package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.LWWMapKey;
import akka.cluster.ddata.Replicator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Udp;
import com.paltaie.akkatftpserver.model.ReadRequest;
import org.apache.commons.lang.ArrayUtils;
import scala.collection.JavaConversions;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ReadRequestActor extends AbstractActor {

    private static final Byte ZERO_BYTE = Byte.valueOf("0");
    static final Key<LWWMap<InetSocketAddress, ReadRequest>> MAP_KEY = LWWMapKey.create("key");

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Cluster cluster = Cluster.get(context().system());
    private final ActorRef replicator =
            DistributedData.get(getContext().getSystem()).replicator();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, this::handleReceived)
                .match(Replicator.UpdateSuccess.class, this::handleUpdateSuccess)
                .build();
    }

    private void handleUpdateSuccess(Replicator.UpdateSuccess updateSuccess) {
        log.debug("Update success: {}", updateSuccess);
    }

    private void handleReceived(Udp.Received r) {
        byte[] data = ArrayUtils.toPrimitive(JavaConversions.seqAsJavaList(r.data()).toArray(new Byte[0]));
        String filename = new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 1),  indexOf(data, ZERO_BYTE, 2)), StandardCharsets.UTF_8).trim();
        String mode = new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 2) + 1,  indexOf(data, ZERO_BYTE, 3)), StandardCharsets.UTF_8);
        ReadRequest readRequest = new ReadRequest(TftpOpcode.READ_REQUEST, filename, mode);
        log.info("Storing requester {} with RRQ {} in CRDT store", r.sender(), readRequest);
        Replicator.Update<LWWMap<InetSocketAddress, ReadRequest>> update = new Replicator.Update<>(MAP_KEY, LWWMap.create(), Replicator.writeLocal(), map -> map.put(cluster, r.sender(), readRequest));
        replicator.tell(update, self());
        context().actorOf(Props.create(SegmentSender.class, r.sender(), readRequest, 0)).tell("SEND", sender());
    }

    private int indexOf(byte[] bytes, byte target, int occurrence) {
        int count = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == target) {
                count++;
                if (occurrence == count) {
                    return i;
                }
            }
        }
        return -1;
    }
}
