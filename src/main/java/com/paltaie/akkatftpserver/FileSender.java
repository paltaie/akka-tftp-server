package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.UdpMessage;
import akka.util.ByteString;
import com.paltaie.akkatftpserver.model.Ack;
import com.paltaie.akkatftpserver.model.Data;
import com.paltaie.akkatftpserver.model.Error;
import com.paltaie.akkatftpserver.model.ReadRequest;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileSender extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static final int MAX_BLOCK_SIZE = 512;

    private final InetSocketAddress senderSocket;
    private List<byte[]> blocks;
    private int totalBlocks = 0;
    private int lastAcked = 0;

    public FileSender(InetSocketAddress senderSocket) {
        this.senderSocket = senderSocket;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ReadRequest.class, this::handleReadRequest)
                .build();
    }

    private void handleReadRequest(ReadRequest readRequest) {
        try {
            ClientMap.map.put(senderSocket, self());
            byte[] data = IOUtils.toByteArray(new FileInputStream(new File(readRequest.getFilename())));
            if (data.length <= MAX_BLOCK_SIZE) {
                totalBlocks = 1;
            } else {
                totalBlocks = (data.length / MAX_BLOCK_SIZE) + (data.length % MAX_BLOCK_SIZE > 0 ? 1 : 0);
            }
            blocks = new ArrayList<>(totalBlocks);
            for (int i = 0; i < totalBlocks; i++) {
                if (i == totalBlocks - 1) {//last block
                    blocks.add(Arrays.copyOfRange(data, i*MAX_BLOCK_SIZE, data.length));
                    break;
                }
                blocks.add(Arrays.copyOfRange(data, i*MAX_BLOCK_SIZE, (i*MAX_BLOCK_SIZE)+MAX_BLOCK_SIZE));
            }
            if (data.length % MAX_BLOCK_SIZE == 0) {
                totalBlocks++;
                blocks.add(new byte[]{});
            }
            sender().tell(UdpMessage.send(ByteString.fromArray(new Data(1, blocks.get(0)).getBytes()), senderSocket), self());
            getContext().become(awaitingAck());
        } catch (FileNotFoundException e) {
            Error error = new Error(1, "File not found: " + readRequest.getFilename());
            replyWithError(error);
        } catch (IOException e) {
            Error error = new Error(9, "Unkown error: " + e.getMessage());
            replyWithError(error);
        }
    }

    private void replyWithError(Error error) {
        sender().tell(UdpMessage.send(ByteString.fromArray(error.getBytes()), senderSocket), self());
        context().stop(self());
    }

    private Receive awaitingAck() {
        return receiveBuilder()
                .match(Ack.class, this::handleAck)
                .matchAny(this::unhandled)
                .build();
    }

    private void handleAck(Ack ack) {
        lastAcked = ack.getBlockNumber();
        log.debug("Received an ACK for block " + ack.getBlockNumber() + "/" + totalBlocks + " for consumer " + senderSocket);
        if (lastAcked == totalBlocks) {
            log.debug("All blocks sent and acked. Stopping self.");
//            context().stop(self());
        } else {
            ByteString response = ByteString.fromArray(new Data(lastAcked + 1, blocks.get(lastAcked)).getBytes());
            sender().tell(UdpMessage.send(response, senderSocket), self());
        }
    }
}
