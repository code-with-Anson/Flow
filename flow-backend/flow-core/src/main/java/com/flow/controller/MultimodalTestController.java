package com.flow.controller;

import com.flow.common.context.SakuraIdentify;
import com.flow.model.es.MultimodalAsset;
import com.flow.service.MultimodalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "多模态测试", description = "多模态向量检索测试接口")
@RestController
@RequestMapping("/test/multimodal")
@RequiredArgsConstructor
public class MultimodalTestController {

    private final MultimodalSearchService multimodalSearchService;
    private final com.flow.oss.OssTemplate ossTemplate;

    @Operation(summary = "上传文件并生成向量", description = "支持图片、视频和文本文件的上传，自动生成向量并存储到 Elasticsearch")
    @PostMapping("/upload")
    public MultimodalAsset upload(
            @Parameter(description = "要上传的文件（支持图片/视频/文本）", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件描述信息", example = "这是一张美丽的风景照") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "向量模型名称（留空使用默认模型）", example = "qwen2.5-vl-embedding") @RequestParam(value = "model", required = false) String model) {
        // 从上下文获取当前登录用户ID
        String userId = String.valueOf(SakuraIdentify.getCurrentUserId());
        return multimodalSearchService.uploadAndIndex(file, description, userId, model);
    }

    @Operation(summary = "多模态向量检索", description = "根据文本、图片URL或视频URL进行相似度检索")
    @GetMapping("/search")
    public List<MultimodalAsset> search(
            @Parameter(description = "检索查询（文本内容或图片/视频URL）", required = true, example = "美丽的风景") @RequestParam("query") String query,
            @Parameter(description = "查询类型", example = "text") @RequestParam(value = "type", defaultValue = "text") String type,
            @Parameter(description = "向量模型名称（留空使用默认模型）", example = "qwen2.5-vl-embedding") @RequestParam(value = "model", required = false) String model) {
        return multimodalSearchService.search(query, type, model);
    }

    @Operation(summary = "获取文件外部访问链接", description = "生成 MinIO 文件的外部预签名访问链接（如配置了内网穿透则返回外网地址）")
    @GetMapping("/url")
    public String getExternalUrl(
            @Parameter(description = "文件路径", required = true, example = "merged/example.jpg") @RequestParam("fileName") String fileName) {
        return ossTemplate.getExternalPresignedUrl(fileName);
    }
}
