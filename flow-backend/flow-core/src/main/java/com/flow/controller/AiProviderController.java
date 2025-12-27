package com.flow.controller;

import com.flow.model.entity.AiProviderConfig;
import com.flow.service.AiProviderService;
import com.flow.common.context.SakuraIdentify;
import com.flow.common.util.AESUtils;
import com.flow.factory.DynamicAiFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/provider")
@Tag(name = "AI 供应商管理", description = "AI 供应商配置接口")
@RequiredArgsConstructor
public class AiProviderController {

    private final AiProviderService aiProviderService;
    private final AESUtils aesUtils;
    private final DynamicAiFactory dynamicAiFactory;

    @Operation(summary = "获取所有供应商")
    @GetMapping
    public List<AiProviderConfig> listProviders() {
        return aiProviderService.list();
    }

    @Operation(summary = "添加/更新供应商")
    @PostMapping
    public boolean saveOrUpdateProvider(@RequestBody AiProviderConfig config) {
        // 新增时设置创建人
        if (config.getId() == null) {
            config.setCreateUser(SakuraIdentify.getCurrentUserId());
        }

        // 处理 API Key 加密
        // 前端可能传回 "******" 表示不修改，此时不更新 key 字段
        String apiKey = config.getApiKey();
        if (StringUtils.hasText(apiKey) && !"******".equals(apiKey)) {
            // 使用 AESUtils 的 isEncrypted 方法判断是否已加密
            if (!aesUtils.isEncrypted(apiKey)) {
                config.setApiKey(aesUtils.encrypt(apiKey));
            }
        } else if ("******".equals(apiKey) && config.getId() != null) {
            // 前端传 ****** 表示不修改，保留原值
            AiProviderConfig existing = aiProviderService.getById(config.getId());
            if (existing != null) {
                config.setApiKey(existing.getApiKey());
            }
        }

        boolean result = aiProviderService.saveOrUpdate(config);

        // 更新成功后，清除对应的 ChatClient 缓存
        if (result && config.getId() != null) {
            dynamicAiFactory.invalidate(config.getId());
        }

        return result;
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public boolean deleteProvider(@PathVariable Long id) {
        boolean result = aiProviderService.removeById(id);
        // 删除后清除缓存
        if (result) {
            dynamicAiFactory.invalidate(id);
        }
        return result;
    }
}
