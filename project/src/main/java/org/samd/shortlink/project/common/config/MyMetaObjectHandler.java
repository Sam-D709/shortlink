package org.samd.shortlink.project.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.samd.shortlink.project.common.util.UserContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// java example
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        strictInsertFill(metaObject, "createtime", LocalDateTime::now, LocalDateTime.class);
        strictInsertFill(metaObject, "updatetime", LocalDateTime::now, LocalDateTime.class);
        strictInsertFill(metaObject, "delflag", () -> 0, Integer.class);
        strictInsertFill(metaObject, "enablestatus", () -> 1, Integer.class);
        strictInsertFill(metaObject, "username", UserContext::getUsername, String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        this.strictUpdateFill(metaObject, "updatetime", LocalDateTime::now, LocalDateTime.class);
    }
}