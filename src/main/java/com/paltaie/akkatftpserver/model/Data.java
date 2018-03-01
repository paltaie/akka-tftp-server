package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import lombok.AllArgsConstructor;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

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
        bytes[2] = intToByteArray(blockNumber)[2];
        bytes[3] = intToByteArray(blockNumber)[3];
        for (int i = 0; i < data.length; i++) {
            bytes[i+4] = data[i];
        }
        return bytes;
    }

    private static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
}
