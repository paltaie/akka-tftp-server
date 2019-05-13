package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import com.paltaie.akkatftpserver.TftpUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Ack implements ByteArrayProvider {
    int blockNumber;

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) 0;
        bytes[1] = (byte) TftpOpcode.ACK.getNumeric();
        bytes[2] = intToTwoBytes(blockNumber)[0];
        bytes[3] = intToTwoBytes(blockNumber)[1];
        return bytes;
    }

    public static Ack initialWRQAck() {
        return new Ack(0);
    }
}
