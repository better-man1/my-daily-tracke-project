package com.dailytracker.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 *
 * 【类的用途】
 * 自定义 RedisTemplate 的序列化策略，解决以下问题：
 * 1. 默认的 RedisTemplate 使用 JDK 序列化，存入 Redis 的值是二进制格式，不可读
 * 2. 配置为 JSON 格式序列化，使 Redis 中的数据人类可读，便于调试和排查问题
 * 3. 支持 Java 8 的日期时间类型（LocalDateTime 等）的正确序列化
 *
 * 【设计思想】
 * Redis 的 key 使用 String 序列化器（人类可读），value 使用 JSON 序列化器（跨语言兼容）。
 * 这种组合是最常用的 Redis 序列化方案。
 *
 * 【在架构中的位置】
 * 属于基础设施层，为缓存层提供 Redis 操作模板。
 *
 * 【相关技术知识点 - Redis】
 * 1. Redis 是什么？
 *    Redis（Remote Dictionary Server）是一个基于内存的键值对存储系统，
 *    常用于缓存、消息队列、会话管理等场景，读写速度极快（微秒级）。
 *
 * 2. RedisTemplate vs StringRedisTemplate
 *    - RedisTemplate<String, Object>：key 和 value 可以是任意类型（需序列化器）
 *    - StringRedisTemplate：key 和 value 只能是 String，适合简单场景
 *
 * 3. 序列化器对比：
 *    - JdkSerializationRedisSerializer：Java 原生序列化，二进制格式，不可读
 *    - StringRedisSerializer：纯字符串，适合 key
 *    - Jackson2JsonRedisSerializer：JSON 格式，可读性好，推荐用于 value
 *
 * @Configuration Spring 配置类注解
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义 RedisTemplate Bean，覆盖 Spring Boot 的默认配置
     *
     * 【方法作用】
     * 创建一个配置了 JSON 序列化策略的 RedisTemplate 实例，
     * 替换 Spring Boot 自动配置的默认 RedisTemplate。
     *
     * 【为什么要自定义？】
     * Spring Boot 自动配置的 RedisTemplate 使用 JDK 序列化，
     * 存入 Redis 的值类似 "\xAC\xED\x00\x05sr\x00..." 这样的二进制数据，
     * 不仅不可读，而且存在跨语言兼容性问题和安全风险。
     *
     * @Bean 注册为 Spring Bean，名称为 "redisTemplate"
     *        其他组件可以通过 @Autowired RedisTemplate<String, Object> redisTemplate 注入使用
     *
     * @param factory Redis 连接工厂，由 Spring Boot 根据 application.yml 中的 Redis 配置自动创建
     *                它负责管理与 Redis 服务器的连接（连接池、连接参数等）
     * @return RedisTemplate<String, Object> 配置好的 Redis 操作模板
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 实例
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // 设置 Redis 连接工厂，RedisTemplate 通过它获取与 Redis 服务器的连接
        template.setConnectionFactory(factory);

        // ========== 配置 Jackson ObjectMapper（JSON 序列化核心） ==========
        ObjectMapper om = new ObjectMapper();

        // 设置所有属性的可见性：让 Jackson 能够访问 Java 对象的所有属性（包括 private）
        // PropertyAccessor.ALL 表示所有类型的属性（字段、getter、setter）
        // JsonAutoDetect.Visibility.ANY 表示所有访问修饰符（public、private、protected）
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用默认类型信息：在 JSON 中包含 Java 类型信息
        // 这样反序列化时能自动还原为原始的 Java 类型
        // 例如：JSON 中会包含 "@class":"com.dailytracker.entity.User" 这样的类型标识
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        // NON_FINAL 表示只有非 final 类型才会包含类型信息（final 类如 String 不需要）

        // 注册 JavaTimeModule：支持 Java 8 日期时间 API
        // LocalDateTime、LocalDate、ZonedDateTime 等类型需要此模块才能正确序列化
        om.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳（毫秒数）的特性
        // 禁用后日期会格式化为 "2024-01-15T10:30:00" 这样的字符串，更加可读
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 创建 Jackson2JsonRedisSerializer，使用上面配置好的 ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(om, Object.class);

        // ========== 配置 Key 的序列化器 ==========
        // Key 使用 StringRedisSerializer，将 key 序列化为普通字符串
        // 例如："user:info:1" 会原样存储在 Redis 中，便于通过 Redis CLI 查看
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);            // 普通 key 的序列化器
        template.setHashKeySerializer(stringSerializer);        // Hash 结构中 field 的序列化器

        // ========== 配置 Value 的序列化器 ==========
        // Value 使用 Jackson2JsonRedisSerializer，将 value 序列化为 JSON 格式
        template.setValueSerializer(serializer);                // 普通 value 的序列化器
        template.setHashValueSerializer(serializer);            // Hash 结构中 value 的序列化器

        // 初始化 RedisTemplate 的属性（必须调用，否则配置可能不生效）
        // 此方法会验证并应用所有配置
        template.afterPropertiesSet();

        return template;
    }
}
