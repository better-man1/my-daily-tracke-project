package com.dailytracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DailyTracker 启动类
 */
@SpringBootApplication
@MapperScan("com.dailytracker.mapper")
public class DailyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyTrackerApplication.class, args);
    }
}
