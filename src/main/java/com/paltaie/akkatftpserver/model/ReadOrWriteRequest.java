package com.paltaie.akkatftpserver.model;

import com.paltaie.akkatftpserver.TftpOpcode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadOrWriteRequest {
    private TftpOpcode tftpOpcode;
    private String filename;
    private String mode;


}
