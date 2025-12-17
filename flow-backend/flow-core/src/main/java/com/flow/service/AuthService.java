package com.flow.service;

import com.flow.common.enums.ErrorCode;
import com.flow.common.exception.BusinessException;
import com.flow.model.dto.LoginSakuraReq;
import com.flow.model.dto.RegisterSakuraReq;
import com.flow.model.entity.User;
import com.flow.security.FlowUserDetails;
import com.flow.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public String login(LoginSakuraReq loginSakuraReq) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginSakuraReq.getUsername(), loginSakuraReq.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        FlowUserDetails userDetails = (FlowUserDetails) authentication.getPrincipal();
        return jwtUtils.generateToken(userDetails.getUser().getId());
    }

    public void register(RegisterSakuraReq registerSakuraReq) {
        if (userService.existsByUsername(registerSakuraReq.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(registerSakuraReq.getUsername());
        user.setPassword(passwordEncoder.encode(registerSakuraReq.getPassword()));
        user.setNickname(registerSakuraReq.getNickname());
        user.setEmail(registerSakuraReq.getEmail());

        userService.save(user);
    }
}
