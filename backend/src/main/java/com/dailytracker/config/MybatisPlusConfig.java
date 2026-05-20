package com.dailytracker.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 *
 * 【类的用途】
 * 该配置类主要完成两件事：
 * 1. 配置 MyBatis-Plus 的分页插件，使分页查询功能生效
 * 2. 实现数据库字段的自动填充（创建时间、更新时间），避免在每次新增/修改时手动设置时间
 *
 * 【设计思想】
 * 利用 MyBatis-Plus 的 MetaObjectHandler 接口实现自动填充功能，将公共字段（如创建时间、更新时间）
 * 的填充逻辑统一管理，减少重复代码。这是"约定优于配置"和"DRY（Don't Repeat Yourself）"原则的体现。
 *
 * 【在架构中的位置】
 * 属于基础设施层（Infrastructure Layer）的配置组件，为数据访问层（Mapper层）提供技术支持。
 *
 * 【相关技术知识点】
 * - MyBatis-Plus 是 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变
 * - 分页插件是 MyBatis-Plus 最常用的插件之一，通过拦截 SQL 语句自动添加分页语句
 * - MetaObjectHandler 是 MyBatis-Plus 提供的元数据对象处理器，用于自动填充公共字段
 *
 * @Slf4j Lombok 注解，自动生成日志对象 private static final Logger log = LoggerFactory.getLogger(MybatisPlusConfig.class);
 *         使用时可以直接写 log.info("xxx")、log.warn("xxx") 等
 *
 * @Configuration Spring 注解，标识这是一个配置类，等价于一个 XML 配置文件
 *                  配置类中可以定义 @Bean 方法来声明和注册 Spring 管理的 Bean
 *                  Spring 容器启动时会自动处理这个类中的所有 @Bean 方法
 */
@Slf4j
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    /**
     * 配置 MyBatis-Plus 分页插件
     *
     * 【方法作用】
     * 创建并注册 MyBatis-Plus 拦截器，拦截 SQL 执行并自动添加分页语句（LIMIT/OFFSET）。
     * 没有这个配置，MyBatis-Plus 的 Page 对象将无法正确执行分页查询。
     *
     * 【工作原理】
     * MybatisPlusInterceptor 是一个 SQL 执行拦截器链，内部可以添加多个 InnerInterceptor（内部拦截器）。
     * PaginationInnerInterceptor 是分页拦截器，它会在执行 SQL 前自动改写 SQL：
     *   - 对于 MySQL：在 SQL 末尾追加 LIMIT 语句
     *   - 对于 Oracle：使用 ROWNUM 进行分页
     *   - 对于 PostgreSQL：使用 OFFSET ... LIMIT 语句
     *
     * @Bean 注解告诉 Spring：这个方法的返回值是一个需要由 Spring 容器管理的 Bean
     *         方法名 "mybatisPlusInterceptor" 就是 Bean 的名称
     *         Spring 会自动调用此方法，将返回的对象注册到 IoC 容器中
     *
     * @return MybatisPlusInterceptor 配置好的 MyBatis-Plus 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建拦截器实例
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页拦截器，并指定数据库类型为 MySQL
        // DbType.MYSQL 告诉分页插件使用 MySQL 的分页语法（LIMIT）
        // 如果数据库类型不匹配，生成的分页 SQL 会报错
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    /**
     * 自动填充 - 插入（INSERT）时的字段填充逻辑
     *
     * 【方法作用】
     * 当执行数据库 INSERT 操作时，MyBatis-Plus 会自动调用此方法，
     * 将 createdAt（创建时间）和 updatedAt（更新时间）字段自动设置为当前时间。
     *
     * 【触发条件】
     * 只有在实体类字段上添加了 @TableField(fill = FieldFill.INSERT) 或
     * @TableField(fill = FieldFill.INSERT_UPDATE) 注解的字段才会被填充。
     *
     * 【工作原理】
     * MetaObject 是 MyBatis 提供的元数据对象，它封装了实体对象的属性信息。
     * strictInsertFill 方法会检查目标字段是否为 null，如果为 null 才填充（严格模式）。
     *
     * @param metaObject MyBatis 的元数据对象，包含了当前正在操作的实体类的属性信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 获取当前时间，确保 createdAt 和 updatedAt 使用完全相同的时间戳
        LocalDateTime now = LocalDateTime.now();

        // strictInsertFill：严格模式下的插入填充
        // 参数说明：
        //   - metaObject：元数据对象
        //   - "createdAt"：实体类中的字段名（对应数据库的 created_at 列）
        //   - LocalDateTime.class：字段类型
        //   - now：要填充的值
        // "严格"的含义是：只有当该字段为 null 时才填充，不会覆盖已有的值
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);

        // 插入时同时填充 updatedAt，因为新记录的更新时间等于创建时间
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    /**
     * 自动填充 - 更新（UPDATE）时的字段填充逻辑
     *
     * 【方法作用】
     * 当执行数据库 UPDATE 操作时，MyBatis-Plus 会自动调用此方法，
     * 将 updatedAt（更新时间）字段自动更新为当前时间。
     *
     * 【注意】
     * 更新时只填充 updatedAt，不填充 createdAt（创建时间不应被修改）。
     *
     * @param metaObject MyBatis 的元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // strictUpdateFill：严格模式下的更新填充
        // 只填充 updatedAt 字段为当前时间
        // 使用 LocalDateTime.now() 而非复用变量，确保获取的是更新操作的精确时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
