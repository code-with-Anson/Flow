package com.flow.ai.factory;

import com.flow.ai.model.enums.AiProvider;
import com.flow.ai.strategy.AiStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AiStrategyFactory {

    private final Map<AiProvider, AiStrategy> strategyMap;

    public AiStrategyFactory(List<AiStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        strategy -> {
                            // 这里需要遍历所有 Provider 找到支持的那个
                            // 简化起见，我们假设每个 Strategy 只支持一个 Provider，或者在 Strategy 中定义 getProvider()
                            // 但由于 supports() 方法的存在，我们应该这样构建：
                            for (AiProvider provider : AiProvider.values()) {
                                if (strategy.supports(provider)) {
                                    return provider;
                                }
                            }
                            return AiProvider.OPENAI; // Default fallback
                        },
                        Function.identity(),
                        (existing, replacement) -> existing));
    }

    public AiStrategy getStrategy(AiProvider provider) {
        return Optional.ofNullable(strategyMap.get(provider))
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for provider: " + provider));
    }
}
