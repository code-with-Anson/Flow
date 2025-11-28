package com.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.UserMapper;
import com.flow.model.dto.UserSakuraReq;
import com.flow.model.dto.UserQuerySakuraReq;
import com.flow.model.entity.User;
import com.flow.model.vo.UserVO;
import com.flow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean existsByUsername(String username) {
        return baseMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public IPage<UserVO> pageUsers(UserQuerySakuraReq querySakuraReq) {
        Page<User> page = new Page<>(querySakuraReq.getPage(), querySakuraReq.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(querySakuraReq.getUsername()), User::getUsername, querySakuraReq.getUsername())
                .like(StringUtils.hasText(querySakuraReq.getNickname()), User::getNickname,
                        querySakuraReq.getNickname())
                .orderByDesc(User::getCreateTime);

        IPage<User> userPage = baseMapper.selectPage(page, wrapper);

        return userPage.convert(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRoles(baseMapper.selectRoleCodesByUserId(user.getId()));
            return vo;
        });
    }

    @Override
    public boolean saveUser(UserSakuraReq userSakuraReq) {
        if (existsByUsername(userSakuraReq.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        BeanUtils.copyProperties(userSakuraReq, user);
        user.setPassword(passwordEncoder.encode(userSakuraReq.getPassword()));
        return save(user);
    }

    @Override
    public boolean updateUser(UserSakuraReq userSakuraReq) {
        User user = getById(userSakuraReq.getId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        // Prevent updating username and password directly via this method if needed
        BeanUtils.copyProperties(userSakuraReq, user, "password", "username");

        if (StringUtils.hasText(userSakuraReq.getPassword())) {
            user.setPassword(passwordEncoder.encode(userSakuraReq.getPassword()));
        }

        return updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        removeById(id);
    }
}
