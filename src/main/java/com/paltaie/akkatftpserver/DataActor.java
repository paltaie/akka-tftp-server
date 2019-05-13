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
import com.paltaie.akkatftpserver.model.Data;
import com.paltaie.akkatftpserver.model.WriteRequest;
import scala.Option;
import scala.concurrent.duration.Duration;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.paltaie.akkatftpserver.WriteRequestActor.MAP_KEY;

public class DataActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef replicator =
            DistributedData.get(getContext().getSystem()).replicator();

    private InetSocketAddress inetSocketAddress;
    private Ack ack;
    private ActorRef sender;
    private byte[] data;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, a -> {
                    sender = sender();
                    inetSocketAddress = a.sender();
                    ack = mapAck(a);
                    data = mapData(a);
                    log.debug("Received an WRQ for block {} for consumer {}", ack.getBlockNumber(), a.sender());
                    Replicator.ReadMajority readMaj = new Replicator.ReadMajority(Duration.create(1, TimeUnit.SECONDS));
                    replicator.tell(new Replicator.Get<>(MAP_KEY, readMaj), self());
                })
                .match(Replicator.GetSuccess.class, this::handleGetSuccess)
                .build();
    }

    private void handleGetSuccess(Replicator.GetSuccess<LWWMap<InetSocketAddress, WriteRequest>> getSuccess) {
        Option<WriteRequest> readRequestOption = getSuccess.dataValue().get(inetSocketAddress);
        if (readRequestOption.nonEmpty()) {

            context().actorOf(Props.create(SegmentReceiver.class, inetSocketAddress, readRequestOption.get(), new Data(ack.getBlockNumber(), data)))
                    .tell("WRITE", sender);
        }
    }

    private Ack mapAck(Udp.Received a) {
        byte[] data = TftpUtils.receivedToByteArray(a);
        return new Ack(ByteBuffer.wrap(Arrays.copyOfRange(data, 2, 4)).getShort());
    }

    private byte[] mapData(Udp.Received received) {
        byte[] data = TftpUtils.receivedToByteArray(received);
        return Arrays.copyOfRange(data, 4, data.length);
    }
}
