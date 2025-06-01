package com.example.optbackend.dto;

import lombok.Data;

@Data
public class OtpCodeGetDto {
    private String code;
    private long ttl;
}
