package com.dailytracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DailyTracker 应用程序的主启动类（入口类）
 *
 * 【类的用途】
 * 这是整个 Spring Boot 项目的入口点，Java 应用程序运行的起点。
 * 所有的 Spring 组件扫描、自动配置、Bean 注册等核心机制都从这个类开始。
 *
 * 【在架构中的位置】
 * 位于项目包结构的最顶层（com.dailytracker），这是因为 Spring Boot 的组件扫描机制
 * 会从主启动类所在的包开始，向下扫描所有子包中的 @Component、@Service、@Controller
 * 等注解标记的类，并将它们注册为 Spring 容器中的 Bean。
 *
 * 【核心概念：Spring Boot 启动原理】
 * 1. SpringApplication.run() 方法会创建 Spring 应用上下文（ApplicationContext）
 * 2. 执行自动配置（Auto-Configuration），根据 classpath 中的依赖自动配置 Bean
 * 3. 启动内嵌的 Web 服务器（如 Tomcat），默认监听 8080 端口
 * 4. 扫描并注册所有带有 Spring 注解的组件
 *
 * @SpringBootApplication 是一个组合注解，它包含了以下三个注解的功能：
 *   - @SpringBootConfiguration：标识这是一个配置类，等价于 @Configuration
 *   - @EnableAutoConfiguration：启用 Spring Boot 的自动配置机制
 *   - @ComponentScan：启用组件扫描，自动发现并注册 Spring Bean
 *
 * @MapperScan("com.dailytracker.mapper")
 * MyBatis-Plus 提供的注解，用于扫描指定包下的 MyBatis Mapper 接口。
 * Mapper 接口是数据访问层（DAO层）的核心，每个 Mapper 对应数据库中的一张表。
 * 此注解告诉 MyBatis 在 "com.dailytracker.mapper" 包下查找所有 Mapper 接口，
 * 并自动为它们创建代理实现类，然后注册为 Spring Bean。
 */
@SpringBootApplication
@MapperScan("com.dailytracker.mapper")
public class DailyTrackerApplication {

    /**
     * Java 应用程序的入口方法
     *
     * @param args 命令行参数，可以在启动时传递配置参数，例如：--server.port=9090
     *
     * 【运行流程】
     * SpringApplication.run() 方法的两个参数：
     *   1. DailyTrackerApplication.class - 主配置类（通常是启动类本身）
     *   2. args - 命令行参数
     *
     * 该方法内部执行的操作：
     *   1. 创建 Bootstrap 上下文
     *   2. 准备环境（读取配置文件 application.yml/properties）
     *   3. 创建 ApplicationContext
     *   4. 预处理 ApplicationContext（注册 BeanDefinition）
     *   5. 刷新 ApplicationContext（实例化所有单例 Bean）
     *   6. 执行 Runner（CommandLineRunner / ApplicationRunner）
     *   7. 发布应用启动完成事件
     */
    public static void main(String[] args) {
        SpringApplication.run(DailyTrackerApplication.class, args);
    }
}
