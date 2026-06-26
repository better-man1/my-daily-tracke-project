# Spring Boot 技术知识点总结

## 1. Spring Boot 是什么

Spring Boot 是 Spring 生态中的快速应用开发框架，用于创建可独立运行、生产可用的 Spring 应用。它通过自动配置、Starter 依赖、内嵌服务器、外部化配置和生产级监控能力，大幅降低了传统 Spring 项目的配置成本。

Spring Boot 的核心目标：

- 快速创建 Spring 应用。
- 减少 XML 和繁琐配置。
- 提供约定优于配置的默认实践。
- 内置 Tomcat、Jetty、Undertow 等服务器。
- 提供 Starter 简化依赖管理。
- 提供 Actuator 支持健康检查、指标、监控。
- 支持外部化配置，便于多环境部署。
- 支持传统 Jar、War、容器镜像、云平台等部署方式。

Spring Boot 常见应用场景：

- RESTful API 服务。
- Web 后端应用。
- 企业管理系统后端。
- 微服务。
- 定时任务服务。
- 消息消费服务。
- BFF 服务。
- 数据处理服务。
- 云原生应用。

## 2. Spring Boot 与 Spring Framework 的关系

Spring Framework 是基础框架，提供 IoC、AOP、事务、Web MVC、数据访问、事件、资源管理等核心能力。

Spring Boot 并不是替代 Spring Framework，而是在 Spring Framework 之上提供快速开发和生产化能力。

可以这样理解：

```text
Spring Framework 提供核心能力
Spring Boot 提供自动配置、启动器、内嵌服务器和生产化能力
Spring Cloud 提供分布式和微服务治理能力
```

Spring Boot 主要解决传统 Spring 项目中的问题：

- 配置文件多。
- 依赖版本难管理。
- Web 容器部署麻烦。
- 项目初始化成本高。
- 环境配置分散。
- 生产监控能力需要自行集成。

## 3. Spring Boot 核心特性

### 3.1 自动配置

自动配置是 Spring Boot 的核心能力之一。

它会根据：

- classpath 中是否存在某些类。
- 当前是否已有某些 Bean。
- 配置文件中是否设置某些属性。
- 当前应用类型。

自动创建合适的 Bean 和默认配置。

例如：

- 引入 `spring-boot-starter-web` 后，会自动配置 Spring MVC、Jackson、Tomcat。
- 引入数据库驱动和 JPA 后，会自动配置 DataSource、EntityManagerFactory、事务管理器。
- 引入 Redis Starter 后，会自动配置 RedisConnectionFactory、RedisTemplate。

自动配置的价值：

- 减少样板配置。
- 降低入门门槛。
- 让常见场景开箱即用。

但高级开发者需要理解：

- 自动配置不是魔法。
- 它是基于条件注解和配置类实现的。
- 当默认配置不满足需求时，可以通过配置属性、自定义 Bean 或排除自动配置进行覆盖。

### 3.2 Starter 依赖

Starter 是 Spring Boot 提供的一组依赖聚合。

常见 Starter：

- `spring-boot-starter-web`
- `spring-boot-starter-webflux`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-jdbc`
- `spring-boot-starter-data-redis`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `spring-boot-starter-test`

Starter 的价值：

- 简化依赖引入。
- 自动引入相关库。
- 使用 Spring Boot 统一管理版本。
- 减少依赖冲突。

示例：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 3.3 内嵌服务器

Spring Boot Web 应用通常内嵌 Servlet 容器。

常见内嵌服务器：

- Tomcat。
- Jetty。
- Undertow。

默认使用 Tomcat。

优势：

- 应用可以直接通过 `java -jar` 启动。
- 不需要单独部署到外部 Tomcat。
- 更适合容器化和云原生部署。
- 发布包自包含，环境一致性更好。

启动方式：

```bash
java -jar app.jar
```

### 3.4 外部化配置

Spring Boot 支持把配置从代码中分离出来。

常见配置来源：

- `application.properties`
- `application.yml`
- 环境变量。
- JVM 系统属性。
- 命令行参数。
- 配置中心。
- Profile 专用配置文件。

示例：

```yaml
server:
  port: 8080

spring:
  application:
    name: user-service
```

价值：

- 不同环境使用不同配置。
- 避免硬编码。
- 方便部署。
- 配置变更更清晰。

### 3.5 生产级特性

Spring Boot Actuator 提供生产可用能力。

常见能力：

- 健康检查。
- 应用信息。
- 指标。
- 日志级别管理。
- Bean 查看。
- 环境变量查看。
- 线程 dump。
- Prometheus 指标暴露。

常见端点：

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`
- `/actuator/loggers`

## 4. 项目结构

典型 Spring Boot 项目结构：

```text
src
├─ main
│  ├─ java
│  │  └─ com.example.demo
│  │     ├─ DemoApplication.java
│  │     ├─ controller
│  │     ├─ service
│  │     ├─ repository
│  │     ├─ entity
│  │     ├─ dto
│  │     ├─ config
│  │     ├─ exception
│  │     └─ common
│  └─ resources
│     ├─ application.yml
│     ├─ static
│     └─ templates
└─ test
   └─ java
```

目录职责：

- `controller`：处理 HTTP 请求。
- `service`：业务逻辑。
- `repository`：数据访问。
- `entity`：数据库实体。
- `dto`：接口入参和出参对象。
- `config`：配置类。
- `exception`：异常处理。
- `common`：通用工具和基础类。

推荐原则：

- Controller 不写复杂业务逻辑。
- Service 处理核心业务规则。
- Repository 只负责数据访问。
- DTO 和 Entity 不要无脑混用。
- 配置类按职责拆分。

## 5. 启动入口

Spring Boot 应用通常从带有 `@SpringBootApplication` 的类启动。

示例：

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

`@SpringBootApplication` 是组合注解，主要包含：

- `@SpringBootConfiguration`
- `@EnableAutoConfiguration`
- `@ComponentScan`

含义：

- 当前类是配置类。
- 开启自动配置。
- 扫描当前包及子包下的组件。

注意：

- 启动类建议放在根包下。
- 如果启动类放错位置，可能导致 Controller、Service、Repository 扫描不到。

## 6. IoC 与依赖注入

### 6.1 IoC 是什么

IoC 是 Inversion of Control，控制反转。

传统方式：

```java
UserService userService = new UserService();
```

Spring 方式：

```java
@Service
public class UserService {
}
```

对象创建和依赖管理交给 Spring 容器。

价值：

- 降低对象之间耦合。
- 统一管理生命周期。
- 便于测试和替换实现。
- 支持 AOP、事务、代理等能力。

### 6.2 Bean

Bean 是由 Spring 容器管理的对象。

常见声明方式：

```java
@Component
@Service
@Repository
@Controller
@RestController
@Configuration
```

也可以使用 `@Bean`：

```java
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
```

### 6.3 依赖注入方式

推荐构造器注入：

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

优点：

- 依赖明确。
- 支持 final。
- 更容易测试。
- 避免对象处于半初始化状态。

不推荐大量使用字段注入：

```java
@Autowired
private UserRepository userRepository;
```

字段注入的问题：

- 依赖不明显。
- 不利于单元测试。
- 不能使用 final。

## 7. 自动配置原理

### 7.1 条件注解

自动配置大量使用条件注解。

常见条件注解：

- `@ConditionalOnClass`
- `@ConditionalOnMissingBean`
- `@ConditionalOnBean`
- `@ConditionalOnProperty`
- `@ConditionalOnWebApplication`
- `@ConditionalOnMissingClass`

示例含义：

- classpath 中有某个类才配置。
- 容器中没有某个 Bean 才创建默认 Bean。
- 配置文件中开启某个属性才生效。

### 7.2 覆盖默认配置

常见方式：

1. 修改配置属性。
2. 自己定义同类型 Bean。
3. 排除自动配置。

排除自动配置：

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DemoApplication {
}
```

建议：

- 优先使用配置属性。
- 再考虑自定义 Bean。
- 最后才排除自动配置。

### 7.3 查看自动配置报告

可以通过：

- 启动日志。
- Actuator `/actuator/conditions`。
- debug 模式。

帮助分析：

- 哪些自动配置生效。
- 哪些自动配置没有生效。
- 为什么没有生效。

## 8. 配置文件与 Profile

### 8.1 application.yml

示例：

```yaml
server:
  port: 8080

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:mysql://localhost:3306/app
    username: root
    password: root
```

YAML 优点：

- 层级清晰。
- 可读性好。

注意：

- 缩进必须正确。
- 不要混用 Tab。
- 敏感配置不要提交到仓库。

### 8.2 Profile

Profile 用于不同环境配置。

常见文件：

```text
application.yml
application-dev.yml
application-test.yml
application-prod.yml
```

激活方式：

```yaml
spring:
  profiles:
    active: dev
```

或命令行：

```bash
java -jar app.jar --spring.profiles.active=prod
```

### 8.3 @ConfigurationProperties

用于将配置绑定成类型安全对象。

```yaml
app:
  upload:
    max-size: 10MB
    path: /data/uploads
```

```java
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private String maxSize;
    private String path;

    // getters and setters
}
```

价值：

- 避免到处使用 `@Value`。
- 配置集中管理。
- 支持元数据提示。
- 更适合复杂配置。

## 9. Spring MVC 与 REST API

### 9.1 Controller

REST Controller 示例：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }
}
```

常见注解：

- `@RestController`
- `@Controller`
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@PatchMapping`
- `@DeleteMapping`
- `@PathVariable`
- `@RequestParam`
- `@RequestBody`
- `@RequestHeader`

### 9.2 RESTful API 设计

示例：

```text
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

建议：

- 使用名词表示资源。
- 使用 HTTP 方法表示动作。
- 返回统一响应结构。
- 错误码清晰。
- 分页、排序、过滤参数规范。
- 状态变更接口考虑幂等性。

### 9.3 参数绑定

路径参数：

```java
@GetMapping("/{id}")
public User get(@PathVariable Long id) {
    return userService.get(id);
}
```

查询参数：

```java
@GetMapping
public List<User> list(@RequestParam String keyword) {
    return userService.search(keyword);
}
```

请求体：

```java
@PostMapping
public User create(@RequestBody CreateUserRequest request) {
    return userService.create(request);
}
```

### 9.4 统一响应结构

示例：

```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
}
```

价值：

- 前端处理一致。
- 错误格式统一。
- 方便扩展 requestId、traceId。

注意：

- 不要让 HTTP 状态码完全失去意义。
- 业务错误和系统错误要区分。

## 10. 参数校验

### 10.1 Bean Validation

引入：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

示例：

```java
public class CreateUserRequest {

    @NotBlank
    private String username;

    @Email
    private String email;

    @Min(18)
    private Integer age;
}
```

Controller：

```java
@PostMapping
public User create(@Valid @RequestBody CreateUserRequest request) {
    return userService.create(request);
}
```

常见注解：

- `@NotNull`
- `@NotBlank`
- `@NotEmpty`
- `@Size`
- `@Min`
- `@Max`
- `@Email`
- `@Pattern`
- `@Positive`
- `@Past`
- `@Future`

### 10.2 分组校验

适合创建和更新使用不同规则。

```java
public interface CreateGroup {}
public interface UpdateGroup {}
```

```java
@NotNull(groups = UpdateGroup.class)
private Long id;
```

### 10.3 自定义校验

适合复杂业务校验，例如手机号格式、枚举值、业务编码。

核心步骤：

1. 定义注解。
2. 实现 ConstraintValidator。
3. 在字段上使用注解。

注意：

- 参数格式校验适合放在校验层。
- 依赖数据库的业务校验通常放在 Service 层更清晰。

## 11. 异常处理

### 11.1 全局异常处理

使用 `@RestControllerAdvice`。

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }
}
```

常见处理：

- 参数校验异常。
- 业务异常。
- 权限异常。
- 数据不存在。
- 系统异常。

### 11.2 业务异常

```java
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
```

建议：

- 业务异常应带明确错误码。
- 不要直接把系统异常堆栈返回给用户。
- 日志中保留排查上下文。

### 11.3 错误码设计

错误码应：

- 可分类。
- 可定位。
- 可文档化。
- 前后端一致。

示例：

```text
10001 用户不存在
10002 用户名已存在
20001 订单不存在
20002 订单状态不允许取消
```

## 12. 数据访问

### 12.1 JDBC

Spring Boot 可以通过 `JdbcTemplate` 访问数据库。

适合：

- 简单 SQL。
- 对 SQL 控制要求高。
- 轻量数据访问。

示例：

```java
List<User> users = jdbcTemplate.query(
    "select id, name from users",
    (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("name"))
);
```

### 12.2 Spring Data JPA

Spring Data JPA 简化基于 JPA 的数据访问。

实体：

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
}
```

Repository：

```java
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
```

优点：

- CRUD 简单。
- 方法名派生查询。
- 分页排序方便。
- 与事务集成好。

注意：

- 复杂查询要关注 SQL。
- 避免 N+1 查询。
- 实体关系映射不要过度复杂。
- 批量写入和复杂报表可能不适合 JPA 直接处理。

### 12.3 MyBatis

MyBatis 是常见 SQL 映射框架。

适合：

- SQL 控制要求高。
- 复杂查询。
- 国内企业项目。

特点：

- SQL 显式。
- 灵活。
- 易于优化。

注意：

- XML 和接口要保持一致。
- 动态 SQL 过复杂会影响维护。
- 参数要防 SQL 注入。

### 12.4 数据库连接池

Spring Boot 默认常用 HikariCP。

关键参数：

- 最大连接数。
- 最小空闲连接。
- 连接超时。
- 空闲超时。
- 最大生命周期。

注意：

- 连接池过小会导致请求等待。
- 连接池过大会压垮数据库。
- 应结合数据库最大连接数、服务实例数、QPS 估算。

## 13. 事务管理

### 13.1 @Transactional

示例：

```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(CreateOrderRequest request) {
        // 扣库存
        // 创建订单
        // 写日志
    }
}
```

作用：

- 方法执行成功则提交事务。
- 发生符合规则的异常则回滚事务。

### 13.2 事务传播

常见传播行为：

- `REQUIRED`：默认，有事务则加入，没有则创建。
- `REQUIRES_NEW`：创建新事务，挂起当前事务。
- `NESTED`：嵌套事务。
- `SUPPORTS`：有事务就加入，没有也可以。
- `MANDATORY`：必须存在事务。
- `NOT_SUPPORTED`：不使用事务。
- `NEVER`：不能存在事务。

### 13.3 常见事务失效场景

事务可能失效：

- 同类内部方法调用。
- 方法不是 public。
- 异常被捕获没有抛出。
- 默认情况下 checked exception 不回滚。
- 对象不是 Spring 管理的 Bean。

解决：

- 事务方法放在 Spring Bean 中。
- 通过代理对象调用。
- 明确设置 rollbackFor。

```java
@Transactional(rollbackFor = Exception.class)
```

### 13.4 事务使用建议

建议：

- 事务边界放在 Service 层。
- 避免长事务。
- 避免事务中调用慢外部接口。
- 避免事务中做大量计算。
- 并发更新要考虑锁和隔离级别。

## 14. 缓存

### 14.1 Spring Cache

Spring Cache 提供统一缓存抽象。

常见注解：

- `@Cacheable`
- `@CachePut`
- `@CacheEvict`

示例：

```java
@Cacheable(cacheNames = "users", key = "#id")
public User getUser(Long id) {
    return userRepository.findById(id).orElseThrow();
}
```

### 14.2 Redis 集成

引入 Redis Starter 后可使用：

- RedisTemplate。
- StringRedisTemplate。
- Spring Cache Redis。

常见场景：

- 缓存热点数据。
- 分布式锁。
- 限流。
- Session 存储。
- 排行榜。
- 验证码。

### 14.3 缓存问题

常见问题：

- 缓存穿透。
- 缓存击穿。
- 缓存雪崩。
- 缓存和数据库不一致。

解决思路：

- 空值缓存。
- 布隆过滤器。
- 热点 Key 保护。
- 过期时间加随机值。
- 删除缓存而不是更新缓存。
- 延迟双删。
- 消息同步缓存。

## 15. Spring Security

### 15.1 Spring Security 是什么

Spring Security 是 Spring 生态中的安全框架，用于认证、授权和常见安全防护。

常见能力：

- 登录认证。
- 密码加密。
- URL 权限控制。
- 方法级权限。
- CSRF 防护。
- CORS 配置。
- Session 管理。
- OAuth2。
- JWT 资源服务器。

### 15.2 认证与授权

认证 Authentication：

- 判断用户是谁。

授权 Authorization：

- 判断用户能访问什么。

常见模型：

- 用户。
- 角色。
- 权限。
- 资源。

### 15.3 密码加密

密码不能明文存储。

推荐使用：

```java
BCryptPasswordEncoder
```

示例：

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();
String encoded = encoder.encode(rawPassword);
boolean matched = encoder.matches(rawPassword, encoded);
```

### 15.4 JWT

JWT 常用于前后端分离。

流程：

1. 用户登录。
2. 服务端校验用户名密码。
3. 服务端签发 Token。
4. 前端后续请求携带 Token。
5. 服务端验证 Token。

注意：

- Token 过期时间不能过长。
- 不要在 JWT 中放敏感明文。
- 需要考虑刷新 Token、登出、黑名单、密钥轮换。

### 15.5 方法级权限

示例：

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {
}
```

适合：

- Service 层权限控制。
- 细粒度业务权限。

## 16. 日志

### 16.1 日志框架

Spring Boot 默认使用 Logback。

常见日志门面：

- SLF4J。

代码示例：

```java
private static final Logger log = LoggerFactory.getLogger(UserService.class);

log.info("create user: {}", username);
```

### 16.2 日志级别

常见级别：

- TRACE。
- DEBUG。
- INFO。
- WARN。
- ERROR。

生产建议：

- 默认 INFO。
- 排查问题时临时调高指定包日志级别。
- 不要在生产开启大量 DEBUG。

### 16.3 日志实践

建议记录：

- 请求 ID。
- 用户 ID。
- 关键业务参数。
- 外部接口耗时。
- 异常堆栈。
- 状态变化。

避免：

- 打印密码。
- 打印完整 Token。
- 打印银行卡、身份证等敏感信息。
- 过度打印大对象。

## 17. Actuator 与可观测性

### 17.1 Actuator

引入：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

常用端点：

- health。
- info。
- metrics。
- prometheus。
- loggers。
- env。
- beans。
- mappings。
- threaddump。

### 17.2 健康检查

健康检查用于判断服务是否可用。

常见检查：

- 应用是否启动。
- 数据库是否连接。
- Redis 是否可用。
- 磁盘空间。
- 外部依赖。

Kubernetes readiness/liveness 可以使用 health endpoint。

### 17.3 Metrics

Spring Boot 通过 Micrometer 集成指标体系。

常见指标：

- JVM 内存。
- GC。
- 线程。
- HTTP 请求耗时。
- 数据库连接池。
- 缓存。
- 自定义业务指标。

可接入：

- Prometheus。
- Grafana。
- OpenTelemetry。

### 17.4 Tracing

链路追踪用于分析一次请求经过哪些服务和耗时。

关注：

- traceId。
- spanId。
- 下游调用。
- 慢接口。
- 错误链路。

微服务系统中非常重要。

## 18. 测试

### 18.1 单元测试

常用：

- JUnit 5。
- Mockito。

适合测试：

- 工具类。
- Service 业务逻辑。
- 参数校验。
- 状态流转。

示例：

```java
@Test
void shouldCreateUser() {
    // given
    // when
    // then
}
```

### 18.2 Spring Boot Test

`@SpringBootTest` 会启动 Spring 应用上下文。

```java
@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

适合：

- 集成测试。
- 验证 Spring 上下文。
- 多组件协作测试。

注意：

- 启动成本较高。
- 不应所有测试都用 `@SpringBootTest`。

### 18.3 Web 层测试

MockMvc：

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
}
```

适合：

- Controller 测试。
- 参数绑定测试。
- 响应状态测试。

### 18.4 数据层测试

`@DataJpaTest` 用于 JPA 测试。

适合：

- Repository 测试。
- 实体映射测试。
- 查询方法测试。

### 18.5 Testcontainers

Testcontainers 可以在测试中启动真实依赖容器。

适合：

- MySQL。
- PostgreSQL。
- Redis。
- Kafka。
- RabbitMQ。

价值：

- 比 Mock 更接近真实环境。
- 避免本地环境差异。
- 适合集成测试。

## 19. 消息队列

Spring Boot 支持多种消息系统。

常见：

- Kafka。
- RabbitMQ。
- RocketMQ。
- ActiveMQ。
- JMS。

使用场景：

- 异步处理。
- 削峰填谷。
- 事件驱动。
- 数据同步。
- 日志采集。

关键问题：

- 消息丢失。
- 重复消费。
- 顺序消费。
- 消息积压。
- 死信队列。
- 幂等处理。

高级要求：

- 消费者必须幂等。
- 失败要有重试和死信机制。
- 监控消费延迟和积压。
- 关键业务要有补偿和对账。

## 20. 定时任务

### 20.1 @Scheduled

启用：

```java
@EnableScheduling
```

示例：

```java
@Scheduled(cron = "0 0 * * * ?")
public void syncData() {
}
```

适合：

- 简单定时任务。
- 单体应用。
- 低并发调度。

注意：

- 多实例部署可能重复执行。
- 复杂调度需要分布式任务框架。

### 20.2 Quartz

Quartz 适合复杂定时任务。

能力：

- 持久化任务。
- Cron 表达式。
- 任务暂停恢复。
- 集群。

### 20.3 分布式任务注意事项

多实例部署要考虑：

- 分布式锁。
- 任务分片。
- 任务幂等。
- 失败重试。
- 执行日志。

## 21. 文件上传与下载

### 21.1 文件上传

Controller 示例：

```java
@PostMapping("/upload")
public String upload(@RequestParam MultipartFile file) {
    return file.getOriginalFilename();
}
```

关注：

- 文件大小限制。
- 文件类型校验。
- 文件名安全。
- 存储路径。
- 防止覆盖。
- 病毒扫描。
- 访问权限。

### 21.2 文件下载

关注：

- Content-Type。
- Content-Disposition。
- 文件名编码。
- 大文件流式下载。
- 权限校验。

注意：

- 不要把用户输入直接拼接成本地文件路径。
- 防止路径遍历攻击。

## 22. API 文档

常见工具：

- OpenAPI。
- springdoc-openapi。
- Swagger UI。

价值：

- 前后端协作。
- 接口调试。
- 自动生成文档。
- 生成客户端类型。

注意：

- 文档应和代码同步。
- 生产环境是否暴露 Swagger UI 要谨慎。
- 内部接口也要有权限控制。

## 23. 部署

### 23.1 Jar 部署

构建：

```bash
mvn clean package
```

运行：

```bash
java -jar target/app.jar
```

适合：

- 简单部署。
- 传统服务器。
- 小型项目。

### 23.2 Docker 部署

Dockerfile 示例：

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

注意：

- 配置通过环境变量注入。
- 不把敏感配置打进镜像。
- 镜像 tag 使用版本号或 commit hash。

### 23.3 Kubernetes 部署

关注：

- Deployment。
- Service。
- Ingress。
- ConfigMap。
- Secret。
- readinessProbe。
- livenessProbe。
- HPA。

Spring Boot 应用应提供健康检查端点给 Kubernetes。

### 23.4 优雅停机

优雅停机用于避免发布时中断正在处理的请求。

配置示例：

```yaml
server:
  shutdown: graceful
```

意义：

- 停止接收新请求。
- 等待已有请求处理完成。
- 更适合滚动发布。

## 24. 性能优化

### 24.1 启动性能

优化方向：

- 减少不必要依赖。
- 避免启动时加载大量数据。
- 懒加载部分 Bean。
- 检查自动配置。
- 使用合适 JVM 参数。
- 对云原生场景研究 AOT 或 Native Image。

### 24.2 接口性能

优化方向：

- SQL 优化。
- 索引优化。
- 缓存。
- 分页。
- 批处理。
- 异步化。
- 减少远程调用。
- 连接池调优。

### 24.3 JVM 性能

关注：

- 堆内存。
- GC。
- 线程数。
- CPU。
- 类加载。
- 直接内存。

工具：

- jcmd。
- jstack。
- jmap。
- VisualVM。
- Arthas。
- JFR。

### 24.4 线程池

线程池用于控制并发资源。

关注参数：

- 核心线程数。
- 最大线程数。
- 队列大小。
- 拒绝策略。
- 线程命名。

注意：

- 不要无界队列堆积任务。
- 不要把所有异步任务共用一个线程池。
- 线程池要按业务隔离。

## 25. 安全实践

### 25.1 输入校验

所有外部输入都不可信。

包括：

- 请求参数。
- 请求体。
- Header。
- Cookie。
- 文件名。
- 第三方回调。

### 25.2 SQL 注入防护

建议：

- 使用参数化查询。
- 使用 ORM 或 MyBatis 参数绑定。
- 不拼接用户输入到 SQL。

危险：

```java
"select * from users where name = '" + name + "'"
```

### 25.3 XSS 和 CSRF

后端需要：

- 输出时正确编码。
- 富文本内容过滤。
- Cookie 设置 HttpOnly、Secure、SameSite。
- 对 Cookie 登录态系统启用 CSRF 防护。

### 25.4 接口权限

必须在服务端校验权限。

前端隐藏按钮不是安全控制。

服务端应校验：

- 用户是否登录。
- 是否有接口权限。
- 是否有数据权限。
- 是否能操作该资源。

## 26. 常见最佳实践

### 26.1 分层清晰

推荐：

```text
Controller -> Service -> Repository
```

不要：

- Controller 直接访问数据库。
- Repository 写业务规则。
- Service 混杂 HTTP 细节。

### 26.2 DTO 与 Entity 分离

Entity 是数据库模型。

DTO 是接口传输模型。

分离价值：

- 避免暴露数据库字段。
- 避免接口和数据库强耦合。
- 方便参数校验。
- 方便版本演进。

### 26.3 统一异常和响应

所有接口应有一致错误格式。

便于：

- 前端处理。
- 日志排查。
- 接口文档。
- 监控统计。

### 26.4 配置外部化

不要硬编码：

- 数据库地址。
- Redis 地址。
- 密钥。
- 第三方 API。
- 文件路径。

### 26.5 生产环境关闭危险端点

Actuator 端点要谨慎暴露。

建议：

- 只开放必要端点。
- 加权限保护。
- 内网访问。
- 不暴露敏感环境变量。

## 27. 常见问题排查

### 27.1 应用启动失败

排查：

- 端口是否被占用。
- 配置文件是否正确。
- Bean 是否冲突。
- 数据库是否可连接。
- 依赖版本是否冲突。
- Profile 是否正确。

### 27.2 Bean 找不到

可能原因：

- 启动类包位置错误。
- 类没有加注解。
- 条件配置未生效。
- Bean 名称冲突。
- Profile 不匹配。

### 27.3 接口 404

可能原因：

- 路径写错。
- Controller 未扫描。
- context-path 配置。
- HTTP 方法不匹配。
- 网关转发路径错误。

### 27.4 接口 500

排查：

- 后端异常日志。
- 参数绑定。
- 数据库异常。
- 空指针。
- 事务异常。
- 外部接口失败。

### 27.5 数据库连接池耗尽

可能原因：

- 慢 SQL。
- 连接未释放。
- 连接池过小。
- 事务太长。
- 并发过高。

解决：

- 优化 SQL。
- 调整连接池。
- 缩短事务。
- 增加监控。
- 限流保护。

## 28. 学习路线建议

推荐学习顺序：

1. Java 基础和面向对象。
2. Maven 或 Gradle。
3. Spring IoC 和 AOP。
4. Spring Boot 启动和 Starter。
5. 配置文件和 Profile。
6. Spring MVC 和 REST API。
7. 参数校验和全局异常。
8. 数据访问：JDBC、JPA、MyBatis。
9. 事务管理。
10. Redis 缓存。
11. Spring Security。
12. 日志和 Actuator。
13. 单元测试和集成测试。
14. 消息队列和定时任务。
15. Docker 和 Kubernetes 部署。
16. 性能优化和线上排障。
17. 微服务和 Spring Cloud。

## 29. 能力自检清单

你应该能够回答：

- Spring Boot 和 Spring Framework 有什么关系？
- `@SpringBootApplication` 包含哪些能力？
- 自动配置是如何生效的？
- Starter 的作用是什么？
- Bean 的生命周期大致是什么？
- 构造器注入为什么优于字段注入？
- `@ConfigurationProperties` 和 `@Value` 如何选择？
- `@RestController` 和 `@Controller` 有什么区别？
- 如何做统一异常处理？
- JPA 和 MyBatis 如何选择？
- `@Transactional` 什么时候会失效？
- Redis 缓存有哪些常见问题？
- Spring Security 如何做认证授权？
- Actuator 有哪些常用端点？
- `@SpringBootTest` 和 `@WebMvcTest` 有什么区别？
- Spring Boot 如何部署到 Docker？
- 生产环境如何做健康检查和优雅停机？

## 30. 官方资料入口

- Spring Boot 官方文档  
  https://docs.spring.io/spring-boot/index.html

- Spring Boot Starters  
  https://docs.spring.io/spring-boot/reference/using/build-systems.html

- Spring Boot Externalized Configuration  
  https://docs.spring.io/spring-boot/reference/features/external-config.html

- Spring Boot Web  
  https://docs.spring.io/spring-boot/reference/web/index.html

- Spring Boot SQL Databases  
  https://docs.spring.io/spring-boot/reference/data/sql.html

- Spring Boot Security  
  https://docs.spring.io/spring-boot/reference/security/index.html

- Spring Boot Actuator  
  https://docs.spring.io/spring-boot/reference/actuator/index.html

- Spring Boot Testing  
  https://docs.spring.io/spring-boot/reference/testing/index.html

## 31. 总结

Spring Boot 的价值在于把 Spring 应用开发从繁琐配置中解放出来，让开发者更专注业务实现，同时提供生产级应用所需的配置、监控、部署和扩展能力。

学习 Spring Boot 不能只停留在会写 Controller，还要理解自动配置、依赖注入、配置体系、事务、数据访问、安全、缓存、测试、监控和部署。真正掌握 Spring Boot 后，你应该能够独立设计和实现一个可维护、可测试、可部署、可监控的后端服务，并能在生产环境中定位问题和持续优化。
