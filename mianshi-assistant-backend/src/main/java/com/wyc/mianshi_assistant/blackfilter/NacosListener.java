package com.wyc.mianshi_assistant.blackfilter;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 黑名单监听器
 */
@Component
@Slf4j
public class NacosListener implements InitializingBean {

    /**
     * nacos 配置服务，用于与 nacos 配置中心进行交互
     */
    @NacosInjected
    private ConfigService configService;

    @Value("${nacos.config.data-id}")
    private String dataId;

    @Value("${nacos.config.group}")
    private String group;

    /**
     * 当这个 Bean 的所有属性值都注入之后调用这个方法
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("nacos 监听器启动");

        String config = configService.getConfigAndSignListener(dataId, group, 3000L, new Listener() {
            final ThreadFactory threadFactory = new ThreadFactory() {
                private final AtomicInteger poolNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("refresh-ThreadPool" + poolNumber.getAndIncrement());
                    return thread;
                }
            };
            final ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);

            // 通过线程池异步处理黑名单变化的逻辑
            @Override
            public Executor getExecutor() {
                return executorService;
            }

            // 当黑名单发生变化时执行的操作
            @Override
            public void receiveConfigInfo(String configInfo) {
                try {
                    log.info("监听到配置信息变化{}", configInfo);
                    BlackIpUtils.rebuildBlackIpList(configInfo);
                } catch (Exception e) {
                    log.error("处理配置信息时发生异常", e);
                }
            }
        });

        // 初始化黑名单
        BlackIpUtils.rebuildBlackIpList(config);
    }
}
