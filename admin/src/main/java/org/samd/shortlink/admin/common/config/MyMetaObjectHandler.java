package org.samd.shortlink.admin.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// java example
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        this.strictInsertFill(metaObject, "createtime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatetime", LocalDateTime::now, LocalDateTime.class);
        strictInsertFill(metaObject, "delflag", () -> 0, Integer.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        this.strictInsertFill(metaObject, "updatetime", LocalDateTime::now, LocalDateTime.class);
    }
}