package com.flow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.flow.common.result.SakuraReply;
import com.flow.model.dto.UserDTO;
import com.flow.model.dto.UserQueryDTO;
import com.flow.model.entity.User;
import com.flow.model.vo.UserVO;
import com.flow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户增删改查")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public SakuraReply<UserVO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        UserVO response = new UserVO();
        BeanUtils.copyProperties(user, response);
        return SakuraReply.success(response);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    public SakuraReply<IPage<UserVO>> pageUsers(UserQueryDTO queryDTO) {
        return SakuraReply.success(userService.pageUsers(queryDTO));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public SakuraReply<Void> addUser(@RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return SakuraReply.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public SakuraReply<Void> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return SakuraReply.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public SakuraReply<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return SakuraReply.success();
    }
}
