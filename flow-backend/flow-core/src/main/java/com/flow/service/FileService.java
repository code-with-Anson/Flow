package com.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flow.model.entity.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<File> {
    File upload(MultipartFile file);

    String getPreviewUrl(Long id);

    void removeFile(Long id);

    // Chunked Upload
    boolean checkChunk(String fileMd5, int chunkIndex);

    void uploadChunk(String fileMd5, int chunkIndex, MultipartFile file);

    File mergeChunks(String fileMd5, String fileName, Long totalSize);
}
