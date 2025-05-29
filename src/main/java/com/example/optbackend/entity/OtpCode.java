package com.example.optbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对应数据库表 code_info
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("code_info")
public class OtpCode {

    /** 自增主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 账号 */
    @TableField("account")
    private String account;

    /** 账号ID */
    @TableField("accountId")
    private String accountId;

    /** 算法 */
    @TableField("algorithm")
    private String algorithm;

    /** 应用名 */
    @TableField("appname")
    private String appname;

    /** 位数 */
    @TableField("digits")
    private String digits;

    /** 时间间隔 (注意是 MySQL 保留字，用反引号) */
    @TableField("code_interval")
    private String interval;

    /** 签发者 */
    @TableField("issuer")
    private String issuer;

    /** 密钥 */
    @TableField("secret")
    private String secret;
}
