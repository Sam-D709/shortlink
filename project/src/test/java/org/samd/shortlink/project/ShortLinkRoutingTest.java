package org.samd.shortlink.project;

public class ShortLinkRoutingTest {

    public static void main(String[] args) {
        String fullShortUrl = "www.samd.org/vJH1A";

        // 模拟 HASH_MOD
        int shardingCount = 16;
        int hash = fullShortUrl.hashCode();
        int index = (Math.abs(hash) % shardingCount) + 1;

        System.out.println("路由到表: shortlink2gid_" + index);
        // 确认每次运行结果一致
    }
}
