package com.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flow.model.entity.User;

public interface UserService extends IService<User> {
    boolean existsByUsername(String username);
}
