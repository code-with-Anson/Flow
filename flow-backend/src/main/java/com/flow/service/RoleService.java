package com.flow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.flow.model.dto.RoleDTO;
import com.flow.model.dto.RoleQueryDTO;
import com.flow.model.entity.Role;
import com.flow.model.vo.RoleVO;

import java.util.List;

public interface RoleService extends IService<Role> {
    IPage<RoleVO> pageRoles(RoleQueryDTO queryDTO);

    List<RoleVO> getAllRoles();

    void addRole(RoleDTO roleDTO);

    void updateRole(Long id, RoleDTO roleDTO);

    void deleteRole(Long id);
}
