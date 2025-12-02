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

    private String bucket;

    private String path;

    /**
     * 文件 SHA256/MD5 哈希
     */
    private String fileHash;

    /**
     * 处理状态
     * 0: UPLOADING (上传中/待处理)
     * 1: PROCESSING (AI 处理中/向量化中)
     * 2: COMPLETED (处理完成，可检索)
     * -1: FAILED (处理失败)
     */
    public static final Integer STATUS_UPLOADING = 0;
    public static final Integer STATUS_PROCESSING = 1;
    public static final Integer STATUS_COMPLETED = 2;
    public static final Integer STATUS_FAILED = -1;
    public static final Integer STATUS_QUEUED = 3;

    private Integer status;

    /**
     * 向量库 ID (Qdrant/ES)
     */
    private String vectorId;

    /**
     * 处理失败原因
     */
    private String errorMsg;

    @TableLogic
    private Integer deleted;
}
