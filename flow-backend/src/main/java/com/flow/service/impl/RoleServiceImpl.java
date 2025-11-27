package com.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.RoleMapper;
import com.flow.model.dto.RoleDTO;
import com.flow.model.dto.RoleQueryDTO;
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
    public IPage<RoleVO> pageRoles(RoleQueryDTO queryDTO) {
        Page<Role> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), Role::getName, queryDTO.getName())
                .like(StringUtils.hasText(queryDTO.getCode()), Role::getCode, queryDTO.getCode())
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
    public void addRole(RoleDTO roleDTO) {
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        save(role);
    }

    @Override
    public void updateRole(Long id, RoleDTO roleDTO) {
        Role role = getById(id);
        if (role == null) {
            throw new RuntimeException("Role not found");
        }
        BeanUtils.copyProperties(roleDTO, role);
        updateById(role);
    }

    @Override
    public void deleteRole(Long id) {
        removeById(id);
    }
}
