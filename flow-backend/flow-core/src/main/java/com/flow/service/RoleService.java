package com.flow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.flow.model.dto.RoleSakuraReq;
import com.flow.model.dto.RoleQuerySakuraReq;
import com.flow.model.entity.Role;
import com.flow.model.vo.RoleVO;

import java.util.List;

public interface RoleService extends IService<Role> {
    IPage<RoleVO> pageRoles(RoleQuerySakuraReq querySakuraReq);

    List<RoleVO> getAllRoles();

    boolean saveRole(RoleSakuraReq roleSakuraReq);

    boolean updateRole(RoleSakuraReq roleSakuraReq);

    void deleteRole(Long id);
}
