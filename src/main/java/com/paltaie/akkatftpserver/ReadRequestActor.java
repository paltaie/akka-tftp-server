package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;
import com.paltaie.akkatftpserver.model.Data;
import com.paltaie.akkatftpserver.model.ReadRequest;
import org.apache.commons.io.IOUtils;
import scala.collection.JavaConversions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ReadRequestActor extends AbstractActor {

    private static final Byte ZERO_BYTE = Byte.valueOf("0");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, this::handleReceived)
                .build();
    }

    private void handleReceived(Udp.Received r) throws IOException {
        Byte[] request = JavaConversions.seqAsJavaList(r.data()).toArray(new Byte[0]);
        byte[] data = new byte[request.length];
        for (int i = 0; i < request.length; i++) {
            data[i] = request[i];
        }
        String filename = new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 1),  indexOf(data, ZERO_BYTE, 2)), StandardCharsets.UTF_8).trim();
        String mode = new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 2) + 1,  indexOf(data, ZERO_BYTE, 3)), StandardCharsets.UTF_8);
        ReadRequest readRequest = new ReadRequest(TftpOpcode.READ_REQUEST, filename, mode);
        Data data2 = new Data(1, IOUtils.toByteArray(new FileInputStream(new File(readRequest.getFilename()))));
        ByteString response = ByteString.fromArray(data2.getBytes());
        sender().tell(UdpMessage.send(response, r.sender()), self());
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
