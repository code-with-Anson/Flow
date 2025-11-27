package com.flow.controller;

import com.flow.common.result.SakuraReply;
import com.flow.model.dto.LoginDTO;
import com.flow.model.dto.RegisterDTO;
import com.flow.model.vo.LoginVO;
import com.flow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "用户登录", description = "用户登录")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public SakuraReply<LoginVO> login(@RequestBody LoginDTO request) {
        String token = authService.login(request);
        return SakuraReply.success(new LoginVO(token));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public SakuraReply<Void> register(@RequestBody RegisterDTO request) {
        authService.register(request);
        return SakuraReply.success();
    }
}
