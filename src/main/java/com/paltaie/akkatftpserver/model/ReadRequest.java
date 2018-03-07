package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
public class ReadRequest extends ReadOrWriteRequest implements Serializable {
    public ReadRequest(TftpOpcode tftpOpcode, String filename, String mode) {
        super(tftpOpcode, filename, mode);
    }

}
