package com.example.optbackend.dto;

import lombok.Data;

/**
 * 保存之后，用于转发/返回的 DTO，只包含新生成的主键
 */
@Data
public class OtpFowardDto {
    private Long id;
}