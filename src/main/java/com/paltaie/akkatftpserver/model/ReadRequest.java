package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReadRequest extends ReadOrWriteRequest {
    public ReadRequest(TftpOpcode tftpOpcode, String filename, String mode) {
        super(tftpOpcode, filename, mode);
    }

}
