package com.example.optbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.optbackend.entity.OtpCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * opt_code 表的 Mapper
 */
@Mapper
public interface OtpCodeMapper extends BaseMapper<OtpCode> {
    // 如果以后有自定义查询，也可以在这里声明方法签名
}