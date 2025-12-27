package com.flow.security;

import com.flow.config.mybatis.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof FlowUserDetails) {
                return ((FlowUserDetails) authentication.getPrincipal()).getId();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
