package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.UdpMessage;
import akka.util.ByteString;
import com.paltaie.akkatftpserver.model.Data;
import com.paltaie.akkatftpserver.model.Error;
import com.paltaie.akkatftpserver.model.ReadRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class SegmentSender extends AbstractActor {
    private static final int MAX_BLOCK_SIZE = 512;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final InetSocketAddress inetSocketAddress;
    private final ReadRequest readRequest;
    private final int previousBlockNumber;

    public SegmentSender(InetSocketAddress inetSocketAddress, ReadRequest readRequest, int previousBlockNumber) {
        this.inetSocketAddress = inetSocketAddress;
        this.readRequest = readRequest;
        this.previousBlockNumber = previousBlockNumber;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    private void handleSend() {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(readRequest.getFilename()));
            fileInputStream.skip(previousBlockNumber * MAX_BLOCK_SIZE);
            byte[] bytes = new byte[MAX_BLOCK_SIZE];
            int bytesRead = fileInputStream.read(bytes);
            if (bytesRead == -1) {
                bytes = new byte[0];
            } else if (bytesRead < MAX_BLOCK_SIZE) {
                bytes = Arrays.copyOf(bytes, bytesRead);
            }
            ByteString response = ByteString.fromArray(new Data(previousBlockNumber + 1, bytes).getBytes());
            log.debug("Sending block #{} to {} (size: {}B)", previousBlockNumber + 1, inetSocketAddress, bytes.length);
            sender().tell(UdpMessage.send(response, inetSocketAddress), self());
            fileInputStream.close();
            context().stop(self());
        } catch (FileNotFoundException e) {
            Error error = new Error(1, "File not found: " + readRequest.getFilename());
            replyWithError(error);
        } catch (IOException e) {
            Error error = new Error(9, "Unknown error: " + e.getMessage());
            replyWithError(error);
        }

    }

    private void replyWithError(Error error) {
        sender().tell(UdpMessage.send(ByteString.fromArray(error.getBytes()), inetSocketAddress), self());
        context().stop(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("SEND", send -> handleSend()).build();
    }
}
