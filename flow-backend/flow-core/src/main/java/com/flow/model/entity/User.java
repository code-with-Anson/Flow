package com.flow.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.flow.model.entity.AkatsukiOperator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends AkatsukiOperator {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private Integer status;
    @TableLogic
    private Integer deleted;
}
