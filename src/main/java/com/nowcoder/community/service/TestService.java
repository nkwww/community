package com.nowcoder.community.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    // 可以让该方法在多线程的环境下被异步的调用
    @Async
    public void execute1(){
        logger.debug("execute1");
    }

    // 延迟多少时间执行    定时任务的间隔
    // @Scheduled(initialDelay = 10000, fixedDelay = 1000)
    public void execute2(){
        logger.debug("execute2");
    }
}
