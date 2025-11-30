package com.flow.controller;

import com.flow.common.result.SakuraReply;
import com.flow.model.entity.File;
import com.flow.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public SakuraReply<File> upload(@RequestParam("file") MultipartFile file) {
        return SakuraReply.success(fileService.upload(file));
    }

    @Operation(summary = "获取文件详情")
    @GetMapping("/{id}")
    public SakuraReply<File> getFile(@PathVariable Long id) {
        return SakuraReply.success(fileService.getById(id));
    }

    @Operation(summary = "获取文件预览/下载链接")
    @GetMapping("/download/{id}")
    public SakuraReply<String> getDownloadUrl(@PathVariable Long id) {
        return SakuraReply.success(fileService.getPreviewUrl(id));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public SakuraReply<Void> deleteFile(@PathVariable Long id) {
        fileService.removeFile(id);
        return SakuraReply.success();
    }

    @Operation(summary = "检查分片状态")
    @GetMapping("/chunk")
    public SakuraReply<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
            @RequestParam("chunkIndex") Integer chunkIndex) {
        return SakuraReply.success(fileService.checkChunk(fileMd5, chunkIndex));
    }

    @Operation(summary = "上传分片")
    @PostMapping("/chunk")
    public SakuraReply<Void> uploadChunk(@RequestParam("fileMd5") String fileMd5,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("file") MultipartFile file) {
        fileService.uploadChunk(fileMd5, chunkIndex, file);
        return SakuraReply.success();
    }

    @Operation(summary = "合并分片")
    @PostMapping("/merge")
    public SakuraReply<File> mergeChunks(@RequestParam("fileMd5") String fileMd5,
            @RequestParam("fileName") String fileName,
            @RequestParam("totalSize") Long totalSize) {
        return SakuraReply.success(fileService.mergeChunks(fileMd5, fileName, totalSize));
    }
}
