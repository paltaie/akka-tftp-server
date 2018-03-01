package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Error implements ByteArrayProvider {
    private int number;
    private String message;

    @Override
    public byte[] getBytes() {
        byte[] messageAsBytes = message.getBytes();
        byte[] bytes = new byte[messageAsBytes.length + 4];
        bytes[0] = (byte) 0;
        bytes[1] = (byte) TftpOpcode.ERROR.getNumeric();
        bytes[2] = (byte) 0;
        bytes[3] = (byte) number;
        System.arraycopy(messageAsBytes, 0, bytes, 4, messageAsBytes.length);
        return bytes;
    }
}
