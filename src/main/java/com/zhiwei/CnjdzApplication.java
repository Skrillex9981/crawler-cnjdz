package com.zhiwei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry//开启重试
@EnableScheduling//开启定时任务
public class CnjdzApplication {

    public static void main(String[] args) {
        SpringApplication.run(CnjdzApplication.class, args);
    }

}
