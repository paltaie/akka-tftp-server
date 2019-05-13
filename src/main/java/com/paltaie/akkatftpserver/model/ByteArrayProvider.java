package com.paltaie.akkatftpserver.model;

public interface ByteArrayProvider {
    default byte[] intToTwoBytes(int value) {
        return new byte[] {
            (byte)((value >> 8) & 0xFF),
            (byte)(value & 0xFF)
        };
    }

    byte[] getBytes();
}
