package org.samd.shortlink.admin.common.bloom;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试用例：用于删除 Redis 中的布隆过滤器缓存（慎用，执行会删除对应 key）
 * 运行方式：在 admin 模块中运行 `mvn -DskipTests package` 编译，或直接在 IDE 中运行该测试。
 * 注意：该测试会连接到配置的 Redis 实例，请确保测试环境中 Redis 可用或在运行前修改为 Mock。
 */
@SpringBootTest
@Slf4j
public class BloomFilterCleanupTest {

    @Autowired
    @Qualifier("userRegisterCachePenetrationBloomFilter")
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Test
    public void deleteBloomFilter() {
        assertNotNull(userRegisterCachePenetrationBloomFilter, "RBloomFilter bean should be available");

        try {
            log.info("Attempting to delete bloom filter: {}", userRegisterCachePenetrationBloomFilter.getName());
            boolean deleted = userRegisterCachePenetrationBloomFilter.delete();
            log.info("Delete result: {}", deleted);
        } catch (Exception e) {
            log.error("Failed to delete bloom filter", e);
            throw e;
        }
    }
}
