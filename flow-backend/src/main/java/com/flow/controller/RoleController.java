package com.flow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.flow.common.result.SakuraReply;
import com.flow.model.dto.RoleDTO;
import com.flow.model.dto.RoleQueryDTO;
import com.flow.model.vo.RoleVO;
import com.flow.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@Tag(name = "角色管理", description = "角色增删改查")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    @Operation(summary = "分页查询角色列表")
    public SakuraReply<IPage<RoleVO>> pageRoles(RoleQueryDTO queryDTO) {
        return SakuraReply.success(roleService.pageRoles(queryDTO));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有角色")
    public SakuraReply<List<RoleVO>> getAllRoles() {
        return SakuraReply.success(roleService.getAllRoles());
    }

    @PostMapping
    @Operation(summary = "新增角色")
    public SakuraReply<Void> addRole(@RequestBody RoleDTO roleDTO) {
        roleService.addRole(roleDTO);
        return SakuraReply.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public SakuraReply<Void> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        roleService.updateRole(id, roleDTO);
        return SakuraReply.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public SakuraReply<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return SakuraReply.success();
    }
}
