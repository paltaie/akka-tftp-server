package com.paltaie.akkatftpserver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.Replicator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Udp;
import com.paltaie.akkatftpserver.model.Ack;
import com.paltaie.akkatftpserver.model.ReadRequest;
import org.apache.commons.lang.ArrayUtils;
import scala.Option;
import scala.collection.JavaConversions;
import scala.concurrent.duration.Duration;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.paltaie.akkatftpserver.ReadRequestActor.MAP_KEY;

public class AckActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef replicator =
            DistributedData.get(getContext().getSystem()).replicator();

    private InetSocketAddress inetSocketAddress;
    private Ack ack;
    private ActorRef sender;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, a -> {
                    sender = sender();
                    inetSocketAddress = a.sender();
                    ack = mapAck(a);
                    log.debug("Received an ACK for block {} for consumer {}", ack.getBlockNumber(), a.sender());
                    Replicator.ReadMajority readMaj = new Replicator.ReadMajority(Duration.create(1, TimeUnit.SECONDS));
                    replicator.tell(new Replicator.Get<>(MAP_KEY, readMaj), self());
                })
                .match(Replicator.GetSuccess.class, this::handleGetSuccess)
                .build();
    }

    private void handleGetSuccess(Replicator.GetSuccess<LWWMap<InetSocketAddress, ReadRequest>> getSuccess) {
        Option<ReadRequest> readRequestOption = getSuccess.dataValue().get(inetSocketAddress);
        if (readRequestOption.nonEmpty()) {
            context().actorOf(Props.create(SegmentSender.class, inetSocketAddress, readRequestOption.get(), ack.getBlockNumber())).tell("SEND", sender);
        }
    }

    private Ack mapAck(Udp.Received a) {
        byte[] data = ArrayUtils.toPrimitive(JavaConversions.seqAsJavaList(a.data()).toArray(new Byte[0]));
        return new Ack(ByteBuffer.wrap(Arrays.copyOfRange(data, 2, data.length)).getShort());
    }
}
