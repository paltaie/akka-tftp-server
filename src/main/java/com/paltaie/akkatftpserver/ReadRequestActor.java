package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.io.Udp;
import com.paltaie.akkatftpserver.model.ReadRequest;
import org.apache.commons.lang.ArrayUtils;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ReadRequestActor extends AbstractActor {

    private static final Byte ZERO_BYTE = Byte.valueOf("0");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, this::handleReceived)
                .match(Terminated.class, this::handleTerminated)
                .build();
    }

    private void handleTerminated(Terminated terminated) {
        ClientMap.map.entrySet().stream().filter(a -> a.getValue().equals(terminated.actor())).findFirst().ifPresent(a -> ClientMap.map.remove(a.getKey()));
    }

    private void handleReceived(Udp.Received r) throws IOException {
        byte[] data = ArrayUtils.toPrimitive(JavaConversions.seqAsJavaList(r.data()).toArray(new Byte[0]));
        String filename = new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 1),  indexOf(data, ZERO_BYTE, 2)), StandardCharsets.UTF_8).trim();
        String mode = new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 2) + 1,  indexOf(data, ZERO_BYTE, 3)), StandardCharsets.UTF_8);
        ReadRequest readRequest = new ReadRequest(TftpOpcode.READ_REQUEST, filename, mode);
        ActorRef fileSender = context().actorOf(Props.create(FileSender.class, r.sender()));
        context().watch(fileSender);
        fileSender.tell(readRequest, sender());
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
