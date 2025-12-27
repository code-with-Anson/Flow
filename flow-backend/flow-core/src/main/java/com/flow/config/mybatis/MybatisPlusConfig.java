package com.flow.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler(ObjectProvider<CurrentUserProvider> currentUserProvider) {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "createUser", Long.class, getCurrentUserId());
                this.strictInsertFill(metaObject, "updateUser", Long.class, getCurrentUserId());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictUpdateFill(metaObject, "updateUser", Long.class, getCurrentUserId());
            }

            private Long getCurrentUserId() {
                CurrentUserProvider provider = currentUserProvider.getIfAvailable();
                if (provider != null) {
                    return provider.getCurrentUserId();
                }
                return null;
            }
        };
    }
}
