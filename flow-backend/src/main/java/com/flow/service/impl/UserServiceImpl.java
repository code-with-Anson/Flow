package com.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.UserMapper;
import com.flow.model.entity.User;
import com.flow.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public boolean existsByUsername(String username) {
        return baseMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
}
