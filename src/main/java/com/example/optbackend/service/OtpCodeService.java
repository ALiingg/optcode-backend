package com.example.optbackend.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONArray;
import com.example.optbackend.dto.OtpCodeDto;
import com.example.optbackend.dto.OtpCodeGetDto;
import com.example.optbackend.entity.OtpCode;
import com.example.optbackend.mapper.OtpCodeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OtpCodeService {

    private final OtpCodeMapper OtpCodeMapper;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public OtpCodeService(OtpCodeMapper OtpCodeMapper,
                          ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.OtpCodeMapper = OtpCodeMapper;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 将前端传过来的 JSON 数组解析成 List<optCode>，批量插入数据库，
     * 最后转换成 List<otpCodeDto> 返回给调用方。
     *
     * @param jsonArray 前端传过来的 JSON 数组字符串
     * @return 解析并插入后的 DTO 列表
     */
    public boolean parseAndSave(String jsonArray) {
        try {
            // 1. 反序列化
            List<OtpCode> list = JSONArray.parseArray(jsonArray, OtpCode.class);

            for (OtpCode entity : list) {
                String sql = "INSERT INTO code_info " +
                        "(account, accountId, algorithm, appname, digits, code_interval, issuer, secret) " +
                        "VALUES (?,      ?,         ?,         ?,       ?,      ?,        ?,      ?)";
                jdbcTemplate.update(
                        sql,
                        entity.getAccount(),
                        entity.getAccountId(),
                        entity.getAlgorithm(),
                        entity.getAppname(),
                        entity.getDigits(),
                        entity.getInterval(),
                        entity.getIssuer(),
                        entity.getSecret()
                );
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException("解析或保存 OTP JSON 失败", e);
        }
    }

    public Map<String, OtpCodeGetDto> getAllOptCodes() {
        String sql = "SELECT * FROM code_info";
        // 假设 code_info 表里包含：appname、secret、digits、code_interval（单位：秒）
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        Map<String, OtpCodeGetDto> resp = new HashMap<>();

        long nowMillis = System.currentTimeMillis();
        long nowSec = nowMillis / 1000L;

        for (Map<String, Object> row : result) {
            String appname = row.get("appname").toString();
            String secret = row.get("secret").toString();
            int digits = Integer.parseInt(row.get("digits").toString());
            long codeIntervalSeconds = Long.parseLong(row.get("code_interval").toString());

            // 1) 构造 GoogleAuthenticatorConfig
            GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                    .setCodeDigits(digits)
                    // 将 code_interval（单位：秒）转换为 毫秒
                    .setTimeStepSizeInMillis(codeIntervalSeconds * 1000L)
                    .build();

            GoogleAuthenticator gAuth = new GoogleAuthenticator(config);

            // 2) 生成当前这一时刻的 TOTP
            int currentCode = gAuth.getTotpPassword(secret);
            String codeStr = String.format("%0" + digits + "d", currentCode);

            // 3) 计算剩余秒数：ttlSec
            //    当前完整步数 index = floor(nowSec / codeIntervalSeconds)
            long currentStepIndex = nowSec / codeIntervalSeconds;
            //    下一个窗口起始时间戳（秒）：
            long nextStepStartSec = (currentStepIndex + 1) * codeIntervalSeconds;
            //    剩余秒数
            long ttlSec = nextStepStartSec - nowSec;
            if (ttlSec < 0) {
                ttlSec = 0;
            }

            // 4) 把 code 与 ttl 放到 DTO 里
            OtpCodeGetDto dto = new OtpCodeGetDto();
            dto.setCode(codeStr);
            dto.setTtl(ttlSec);
            resp.put(appname, dto);
        }

        return resp;
    }
    public Boolean changePassword(String username,String oldPassword,String newPassword){
        String sqlGet = "SELECT `password` FROM `user` WHERE `username` = ?";
        try {
            String storedHash = jdbcTemplate.queryForObject(sqlGet, new Object[]{username}, String.class);
            if (!passwordEncoder.matches(oldPassword, storedHash)) {
                // 验证失败
                return false;
            }
        } catch (EmptyResultDataAccessException e) {
            // 用户不存在
            return false;
        }
        String sqlUpdate = "UPDATE `user` SET `password` = ? WHERE `username` = ?";
        jdbcTemplate.update(sqlUpdate, passwordEncoder.encode(newPassword), username);
        return true;
    }
    public String getLevelByUserName(String username){
        String sqlGet = "SELECT `level` FROM `user` WHERE `username` = ?";
        String level;
        try {
            level = jdbcTemplate.queryForObject(sqlGet, new Object[]{username}, String.class);
        } catch (EmptyResultDataAccessException e) {
            // 用户不存在
            return null;
        }
        return level;
    }

    public String login(String username, String password) {
        // 1) 取出数据库里该用户的密码哈希
        String sqlGet = "SELECT `password` FROM `user` WHERE `username` = ?";
        String storedHash;
        try {
            storedHash = jdbcTemplate.queryForObject(sqlGet, new Object[]{username}, String.class);
        } catch (EmptyResultDataAccessException e) {
            // 用户不存在
            return null;
        }

        // 2) 用 BCrypt 去验证
        if (!passwordEncoder.matches(password, storedHash)) {
            // 验证失败
            return null;
        }

        // 3) 验证通过，生成 token 并存库
        String token = UUID.randomUUID().toString();
        String sqlToken = "INSERT INTO `token` (`token`, `username`, `create_time`) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlToken, token, username, DateTime.now());
        return token;
    }

    /**
     * 将实体转换为 DTO
     */
    private OtpCodeDto toDto(OtpCode entity) {
        OtpCodeDto dto = new OtpCodeDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public void register(String username, String rawPassword) {
        String hashed = passwordEncoder.encode(rawPassword);
        String sql = "INSERT INTO user (username, password, level, create_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, username, hashed, "1", DateTime.now());
    }
}
