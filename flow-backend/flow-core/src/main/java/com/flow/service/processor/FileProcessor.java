package com.flow.service.processor;

import com.flow.model.dto.FileProcessingMessage;
import com.flow.model.entity.File;

/**
 * 文件处理策略接口
 */
public interface FileProcessor {
    /**
     * 是否支持该文件类型
     * 
     * @param mimeType 文件 MIME 类型
     * @return true if supported
     */
    boolean supports(String mimeType);

    /**
     * 处理文件
     * 
     * @param file    文件实体
     * @param message 消息上下文
     */
    void process(File file, FileProcessingMessage message);
}
