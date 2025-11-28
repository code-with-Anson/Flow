package com.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.RoleMapper;
import com.flow.model.dto.RoleSakuraReq;
import com.flow.model.dto.RoleQuerySakuraReq;
import com.flow.model.entity.Role;
import com.flow.model.vo.RoleVO;
import com.flow.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public IPage<RoleVO> pageRoles(RoleQuerySakuraReq querySakuraReq) {
        Page<Role> page = new Page<>(querySakuraReq.getPage(), querySakuraReq.getSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(querySakuraReq.getName()), Role::getName, querySakuraReq.getName())
                .like(StringUtils.hasText(querySakuraReq.getCode()), Role::getCode, querySakuraReq.getCode())
                .orderByDesc(Role::getCreateTime);

        IPage<Role> rolePage = baseMapper.selectPage(page, wrapper);

        return rolePage.convert(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        });
    }

    @Override
    public List<RoleVO> getAllRoles() {
        return list().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean saveRole(RoleSakuraReq roleSakuraReq) {
        Role role = new Role();
        BeanUtils.copyProperties(roleSakuraReq, role);
        return save(role);
    }

    @Override
    public boolean updateRole(RoleSakuraReq roleSakuraReq) {
        Role role = new Role();
        BeanUtils.copyProperties(roleSakuraReq, role);
        return updateById(role);
    }

    @Override
    public void deleteRole(Long id) {
        removeById(id);
    }
}
