package com.flow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.flow.model.dto.UserDTO;
import com.flow.model.dto.UserQueryDTO;
import com.flow.model.entity.User;
import com.flow.model.vo.UserVO;

public interface UserService extends IService<User> {
    IPage<UserVO> pageUsers(UserQueryDTO queryDTO);

    void addUser(UserDTO userDTO);

    void updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    boolean existsByUsername(String username);
}
