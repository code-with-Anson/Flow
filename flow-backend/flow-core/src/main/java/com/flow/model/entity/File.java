package com.flow.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class File extends AkatsukiOperator {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String originalName;

    private Long size;

    private String type;

    private String url;

    private String bucket;

    private String path;

    @TableLogic
    private Integer deleted;
}
