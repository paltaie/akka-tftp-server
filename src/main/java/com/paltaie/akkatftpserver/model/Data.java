package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import lombok.AllArgsConstructor;

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
        bytes[2] = (byte) 0;
        bytes[3] = (byte) blockNumber;
        for (int i = 0; i < data.length; i++) {
            bytes[i+4] = data[i];
        }
        return bytes;
    }
}
