package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;

public class WriteRequest extends ReadOrWriteRequest {
    public WriteRequest(TftpOpcode tftpOpcode, String filename, String mode) {
        super(tftpOpcode, filename, mode);
    }
}
