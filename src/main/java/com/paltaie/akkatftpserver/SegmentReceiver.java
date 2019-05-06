package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.UdpMessage;
import akka.util.ByteString;
import com.paltaie.akkatftpserver.model.Ack;
import com.paltaie.akkatftpserver.model.Data;
import com.paltaie.akkatftpserver.model.Error;
import com.paltaie.akkatftpserver.model.WriteRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;

public class SegmentReceiver extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final InetSocketAddress inetSocketAddress;
    private final WriteRequest writeRequest;
    private final Data data;

    public SegmentReceiver(InetSocketAddress inetSocketAddress, WriteRequest writeRequest, Data data) {
        this.inetSocketAddress = inetSocketAddress;
        this.writeRequest = writeRequest;
        this.data = data;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    private void handleWrite() {
        log.info("Writing received data to file {}", writeRequest.getFilename());
        try (FileOutputStream fos = new FileOutputStream(new File(writeRequest.getFilename()), true)) {
            fos.write(data.getData());
            fos.flush();

            sender().tell(UdpMessage.send(ByteString.fromArray(new Ack(data.getBlockNumber()).getBytes()), inetSocketAddress), self());
            context().stop(self());
        } catch (Exception exception) {
            log.error("Error writing to file: " + writeRequest.getFilename(), exception);
            replyWithError(new Error(9, "Unknown error: " + exception.getMessage()));
        }
    }

    private void replyWithError(Error error) {
        sender().tell(UdpMessage.send(ByteString.fromArray(error.getBytes()), inetSocketAddress), self());
        context().stop(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("WRITE", write -> handleWrite()).build();
    }
}
