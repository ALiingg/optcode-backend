// src/main/java/com/example/optbackend/controller/OptCodeController.java
package com.example.optbackend.controller;

import com.baison.e3plus.common.message.Result;
import com.example.optbackend.dto.UserLoginDto;
import com.example.optbackend.dto.UserRegisterDto;
import com.example.optbackend.service.OtpCodeService;
import com.example.optbackend.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/optcodes")
public class OtpCodeController {
    @Autowired
    private final OtpCodeService OtpCodeService;
    @Autowired
    private AuthUtil authUtil;

    public OtpCodeController(OtpCodeService OtpCodeService) {
        this.OtpCodeService = OtpCodeService;
    }

    @PostMapping("/login")
    public ResponseEntity<Result<Boolean>> login(@RequestBody UserLoginDto dto) {
        String token = OtpCodeService.login(dto.getUsername(), dto.getPassword());

        if (token == null) {
            return ResponseEntity.badRequest().body(Result.error("登录失败"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        // 返回 ResponseEntity，包含响应头和结果
        return ResponseEntity.ok()
                .headers(headers)
                .body(Result.success(true));
    }

    @PostMapping("/import")
    public Result<Boolean> importCodes(@RequestBody String jsonArray) {
        return Result.success(OtpCodeService.parseAndSave(jsonArray));
    }

    @GetMapping("/allOptCodes")
    public Result<Map<String,String>> getAllOptCodes() {
        return Result.success(OtpCodeService.getAllOptCodes());
    }
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDto dto) {
        try{
            OtpCodeService.register(dto.getUsername(), dto.getPassword());
            return Result.success();
        }catch (Exception e){
            return Result.error(e.toString());
        }
    }
    @PostMapping("/check")
    public Result<Boolean> check(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("无效的 token");
        }

        String token = authHeader.split(" ")[1];

        try{

            if (authUtil.verifyToken(token) == null){
                return Result.error("无效的 token");
            }
            return Result.success();
        }catch (Exception e){
            return Result.error("无效的token：" + e.toString());
        }
    }
}
