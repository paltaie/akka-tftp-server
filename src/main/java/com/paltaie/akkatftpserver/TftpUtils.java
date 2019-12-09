package com.paltaie.akkatftpserver;

import akka.io.Udp;
import org.apache.commons.lang.ArrayUtils;
import scala.jdk.javaapi.CollectionConverters;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TftpUtils {
    private static final Byte ZERO_BYTE = Byte.valueOf("0");

    public static byte[] receivedToByteArray(Udp.Received received) {
        return ArrayUtils.toPrimitive(CollectionConverters.asJava(received.data()).toArray(new Byte[0]));
    }

    public static String getFileName(Udp.Received received) {
        byte[] data = getReceivedData(received);
        return new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 1), indexOf(data, ZERO_BYTE, 2)), StandardCharsets.UTF_8).trim();
    }

    public static String getMode(Udp.Received received) {
        byte[] data = getReceivedData(received);
        return new String(Arrays.copyOfRange(data, indexOf(data, ZERO_BYTE, 2) + 1,  indexOf(data, ZERO_BYTE, 3)), StandardCharsets.UTF_8);

    }

    public static byte[] getReceivedData(Udp.Received received) {
        return TftpUtils.receivedToByteArray(received);
    }

    private static int indexOf(byte[] bytes, byte target, int occurrence) {
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
