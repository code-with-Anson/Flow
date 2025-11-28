package com.flow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.flow.common.result.SakuraReply;
import com.flow.model.dto.RoleSakuraReq;
import com.flow.model.dto.RoleQuerySakuraReq;
import com.flow.model.vo.RoleVO;
import com.flow.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
    public SakuraReply<IPage<RoleVO>> pageRoles(RoleQuerySakuraReq querySakuraReq) {
        return SakuraReply.success(roleService.pageRoles(querySakuraReq));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有角色")
    public SakuraReply<List<RoleVO>> getAllRoles() {
        return SakuraReply.success(roleService.getAllRoles());
    }

    @PostMapping
    @Operation(summary = "新增角色")
    public SakuraReply<Boolean> addRole(@RequestBody @Validated RoleSakuraReq roleSakuraReq) {
        return SakuraReply.success(roleService.saveRole(roleSakuraReq));
    }

    @PutMapping
    @Operation(summary = "修改角色")
    public SakuraReply<Boolean> updateRole(@RequestBody @Validated RoleSakuraReq roleSakuraReq) {
        return SakuraReply.success(roleService.updateRole(roleSakuraReq));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public SakuraReply<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return SakuraReply.success();
    }
}
