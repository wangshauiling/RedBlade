package com.redblade.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.redblade.auth.security.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器（含组织字段）
 */
@Slf4j
public class CreateAndUpdateMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始insert填充...");
        // 组织编码
        String orgCode = getCurrentOrgCode();
        if (orgCode != null) {
            this.strictInsertFill(metaObject, "orgCode", String.class, orgCode);
        }
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 删除标志
        this.strictInsertFill(metaObject, "delFlag", String.class, "0");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始update填充...");
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 获取当前用户组织编码
     */
    private String getCurrentOrgCode() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                return loginUser.getOrgCode();
            }
        } catch (Exception e) {
            log.debug("获取当前组织编码失败: {}", e.getMessage());
        }
        return null;
    }
}