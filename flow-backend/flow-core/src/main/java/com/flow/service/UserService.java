package com.flow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.flow.model.dto.UserSakuraReq;
import com.flow.model.dto.UserQuerySakuraReq;
import com.flow.model.entity.User;
import com.flow.model.vo.UserVO;

public interface UserService extends IService<User> {
    IPage<UserVO> pageUsers(UserQuerySakuraReq querySakuraReq);

    boolean saveUser(UserSakuraReq userSakuraReq);

    boolean updateUser(UserSakuraReq userSakuraReq);

    void deleteUser(Long id);

    boolean existsByUsername(String username);
}
