package com.flow.model.dto;

import lombok.Data;

@Data
public class RegisterSakuraReq {
    private String username;
    private String password;
    private String nickname;
    private String email;
}
