package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@lombok.Data
@AllArgsConstructor
public class Data implements ByteArrayProvider {
    int blockNumber;
    byte[] data;

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[data.length + 4];
        bytes[0] = (byte) 0;
        bytes[1] = (byte) TftpOpcode.DATA.getNumeric();
        bytes[2] = intToTwoBytes(blockNumber)[0];
        bytes[3] = intToTwoBytes(blockNumber)[1];
        System.arraycopy(data, 0, bytes, 4, data.length);
        return bytes;
    }

    private static byte[] intToTwoBytes(int value) {
        return new byte[] {
            (byte)((value >> 8) & 0xFF),
            (byte)(value & 0xFF)
        };
    }
}
