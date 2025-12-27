package com.flow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.AiProviderMapper;
import com.flow.model.entity.AiProviderConfig;
import com.flow.service.AiProviderService;
import org.springframework.stereotype.Service;

@Service
public class AiProviderServiceImpl extends ServiceImpl<AiProviderMapper, AiProviderConfig>
        implements AiProviderService {
}
