package com.paltaie.akkatftpserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Error {
    private int number;
    private String message;
}
