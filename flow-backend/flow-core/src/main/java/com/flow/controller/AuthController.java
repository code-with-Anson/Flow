package com.flow.controller;

import com.flow.common.result.SakuraReply;
import com.flow.model.dto.LoginSakuraReq;
import com.flow.model.dto.RegisterSakuraReq;
import com.flow.model.vo.LoginVO;
import com.flow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "用户登录", description = "用户登录")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public SakuraReply<LoginVO> login(@RequestBody LoginSakuraReq request) {
        String token = authService.login(request);
        return SakuraReply.success(new LoginVO(token));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public SakuraReply<String> register(@RequestBody @Validated RegisterSakuraReq registerSakuraReq) {
        authService.register(registerSakuraReq);
        return SakuraReply.success("注册成功");
    }
}
