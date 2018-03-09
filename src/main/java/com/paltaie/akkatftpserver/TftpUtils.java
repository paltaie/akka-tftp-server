package com.paltaie.akkatftpserver;

import akka.io.Udp;
import org.apache.commons.lang.ArrayUtils;
import scala.collection.JavaConversions;

public class TftpUtils {
    public static byte[] receivedToByteArray(Udp.Received received) {
        return ArrayUtils.toPrimitive(JavaConversions.seqAsJavaList(received.data()).toArray(new Byte[0]));
    }
}
