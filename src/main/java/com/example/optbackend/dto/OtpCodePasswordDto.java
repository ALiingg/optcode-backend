package com.example.optbackend.dto;

import lombok.Data;

@Data
public class OtpCodePasswordDto {
    private String username;
    private String oldPassword;
    private String newPassword;
}
