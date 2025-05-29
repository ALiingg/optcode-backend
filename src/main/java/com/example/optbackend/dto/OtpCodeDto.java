package com.example.optbackend.dto;

import lombok.Data;

/**
 * 接收前端创建 optCode 时的字段
 */
@Data
public class OtpCodeDto {
    private String account;
    private String accountId;
    private String algorithm;
    private String appname;
    private String digits;
    private String interval;
    private String issuer;
    private String secret;
    private String uuid;
}