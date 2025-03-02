package com.wyc.mianshi_assistant.blackfilter;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

/**
 * 管理黑名单工具类
 */
public class BlackIpUtils {

    /**
     * 布隆过滤器
     */
    private static BitMapBloomFilter bloomFilter;

    /**
     * 判断一个 IP 是否在黑名单中
     *
     * @param ip
     * @return
     */
    public static boolean isBlackIp(String ip) {
        return bloomFilter.contains(ip);
    }

    /**
     * 重建用户黑名单
     *
     * @param configInfo
     */
    public static void rebuildBlackIpList(String configInfo) {
        if(StrUtil.isBlank(configInfo)) {
            configInfo = "{}";
        }
        // 解析 YAML 文件
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(configInfo, Map.class);
        // 获取黑名单
        List<String> blackIpList = (List<String>) map.get("blackIpList");
        // 加锁防止并发
        synchronized (BlackIpUtils.class) {
            if(CollUtil.isNotEmpty(blackIpList)) {
                BitMapBloomFilter newBloomFilter = new BitMapBloomFilter(958506);
                for (String ip : blackIpList) {
                    newBloomFilter.add(ip);
                }
                bloomFilter = newBloomFilter;
            } else {
                bloomFilter = new BitMapBloomFilter(100);
            }
        }
    }

}
