package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Udp;
import com.paltaie.akkatftpserver.model.Ack;
import jnr.x86asm.CONDITION;
import org.apache.commons.lang.ArrayUtils;
import scala.collection.JavaConversions;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class AckActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, a -> {
                    Ack ack = mapAck(a);
                    log.debug("Received an ACK for block {} for consumer {}", ack.getBlockNumber(), a.sender());
                    context().actorOf(Props.create(SegmentSender.class, a.sender(), ClientMap.map2.get(a.sender()), ack.getBlockNumber())).tell("SEND", sender());
                })
                .build();
    }

    private Ack mapAck(Udp.Received a) {
        byte[] data = ArrayUtils.toPrimitive(JavaConversions.seqAsJavaList(a.data()).toArray(new Byte[0]));
        return new Ack(ByteBuffer.wrap(Arrays.copyOfRange(data, 2, data.length)).getShort());
    }
}
