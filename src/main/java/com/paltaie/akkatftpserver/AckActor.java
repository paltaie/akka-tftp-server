package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.io.Udp;
import com.paltaie.akkatftpserver.model.Ack;
import org.apache.commons.lang.ArrayUtils;
import scala.collection.JavaConversions;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class AckActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, a -> ClientMap.map.get(a.sender()).tell(mapAck(a), sender()))
                .build();
    }

    private Object mapAck(Udp.Received a) {
        byte[] data = ArrayUtils.toPrimitive(JavaConversions.seqAsJavaList(a.data()).toArray(new Byte[0]));
        return new Ack(ByteBuffer.wrap(Arrays.copyOfRange(data, 2, data.length)).getShort());
    }
}
