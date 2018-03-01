package com.paltaie.akkatftpserver;

import java.util.Arrays;

public enum TftpOpcode {
    READ_REQUEST(1),
    WRITE_REQUEST(2),
    DATA(3),
    ACK(4),
    ERROR(5);

    private final int numeric;

    TftpOpcode(int numeric) {
        this.numeric = numeric;
    }

    public static TftpOpcode byOpcode(int opcode) {
        return Arrays.stream(TftpOpcode.values())
                .filter(a -> a.getNumeric() == opcode)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unkown opcode: " + opcode + ". Known opcodes: " +
                        Arrays.asList(TftpOpcode.values())));
    }

    public int getNumeric() {
        return numeric;
    }
}
