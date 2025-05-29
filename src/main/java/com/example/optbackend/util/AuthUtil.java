package com.example.optbackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class AuthUtil {

    private final JdbcTemplate jdbcTemplate;

    public AuthUtil(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 校验 token 是否有效：存在且创建时间不晚于 2 天前。
     *
     * @param token 从请求头中拿到的 Bearer token
     * @return token 对应的 username，校验失败返回 null
     */
    public String verifyToken(String token) {
        String sql = "SELECT username, create_time FROM token WHERE token = ?";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, token);

        String username = (String) row.get("username");
        Object timeObj = row.get("create_time");

        if (timeObj == null) {
            log.warn("Token [{}] record has null create_time", token);
            return null;
        }

        LocalDateTime createdAt;
        if (timeObj instanceof LocalDateTime) {
            createdAt = (LocalDateTime) timeObj;
        } else if (timeObj instanceof Timestamp) {
            createdAt = ((Timestamp) timeObj).toLocalDateTime();
        } else {
            log.error("Unexpected type for create_time: {}", timeObj.getClass());
            return null;
        }

        long hours = Duration.between(createdAt, LocalDateTime.now()).toHours();
        if (hours < 2) {
            return username;
        } else {
            log.info("Token [{}] expired: created {} hours ago", token, hours);
            return null;
        }
    }

}
