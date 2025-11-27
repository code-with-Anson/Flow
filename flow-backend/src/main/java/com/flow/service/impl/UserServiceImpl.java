package com.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.UserMapper;
import com.flow.model.dto.UserDTO;
import com.flow.model.dto.UserQueryDTO;
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
    public IPage<UserVO> pageUsers(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername())
                .like(StringUtils.hasText(queryDTO.getNickname()), User::getNickname, queryDTO.getNickname())
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
    public void addUser(UserDTO userDTO) {
        if (existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        save(user);
    }

    @Override
    public void updateUser(Long id, UserDTO userDTO) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        BeanUtils.copyProperties(userDTO, user, "password", "username"); // Prevent updating
                                                                         // username and
                                                                         // password directly
                                                                         // via this method if
                                                                         // needed, or handle
                                                                         // password
                                                                         // separately
        if (StringUtils.hasText(userDTO.getPassword())) {
            // If password update is allowed here
            // user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        removeById(id);
    }
}
