# Spring Cloud 技术知识点总结

## 1. Spring Cloud 是什么

Spring Cloud 是 Spring 生态中用于构建分布式系统和微服务架构的一组工具集合。它不是单一框架，而是多个子项目组成的生态，用来解决微服务系统中常见的工程问题。

Spring Cloud 主要解决：

- 配置中心。
- 服务注册与发现。
- 服务间调用。
- 客户端负载均衡。
- API 网关。
- 熔断与降级。
- 分布式消息。
- 分布式链路追踪。
- 分布式配置刷新。
- 契约测试。
- Kubernetes 集成。
- 云平台适配。

Spring Boot 让单个服务更容易开发和运行，Spring Cloud 则让多个服务之间更容易协作、治理和运维。

可以这样理解：

```text
Spring Framework：基础能力，如 IoC、AOP、事务、Web
Spring Boot：快速构建单个生产级 Spring 应用
Spring Cloud：构建和治理分布式微服务系统
```

## 2. 为什么需要 Spring Cloud

单体应用中，模块之间通常在一个进程内调用，配置、事务、日志、部署都比较集中。

微服务拆分后，会出现新的复杂度：

- 服务数量变多。
- 服务地址动态变化。
- 服务之间需要远程调用。
- 单个服务故障可能影响调用方。
- 配置分散在多个服务中。
- 日志分散在多个实例中。
- 请求链路跨多个服务。
- 服务版本升级需要灰度和回滚。
- 数据一致性变复杂。
- 流量治理变复杂。

Spring Cloud 的价值是把这些通用问题抽象成标准组件，减少每个团队重复造轮子。

## 3. Spring Cloud 整体知识地图

学习 Spring Cloud 需要掌握：

1. 微服务基础。
2. Spring Cloud 版本和 BOM。
3. 服务注册与发现。
4. Spring Cloud Commons。
5. 服务间调用。
6. OpenFeign。
7. 客户端负载均衡。
8. Spring Cloud Gateway。
9. Spring Cloud Config。
10. Spring Cloud Bus。
11. Spring Cloud Circuit Breaker。
12. Resilience4j。
13. Spring Cloud Stream。
14. 消息驱动微服务。
15. Spring Cloud Kubernetes。
16. Spring Cloud Consul、Zookeeper、Eureka。
17. Spring Cloud Contract。
18. Spring Cloud Task。
19. Spring Cloud Function。
20. Spring Cloud Vault。
21. 配置刷新和动态配置。
22. 链路追踪和可观测性。
23. 安全认证与网关鉴权。
24. 分布式事务和一致性。
25. 微服务部署和运维。
26. 常见问题排查。

## 4. Spring Cloud 版本体系

### 4.1 Release Train

Spring Cloud 使用 Release Train 管理一组子项目版本。因为 Spring Cloud 是多个项目组成的生态，各项目版本并不完全一致，所以通过 Release Train BOM 统一管理兼容版本。

例如官方当前体系中：

- `2025.1.x` 对应 Spring Boot 4.0.x。
- `2025.0.x` 对应 Spring Boot 3.5.x。
- `2024.0.x` 对应 Spring Boot 3.4.x。
- `2023.0.x` 对应 Spring Boot 3.3.x、3.2.x。

学习和项目选型时要注意：

- Spring Boot 版本和 Spring Cloud 版本必须兼容。
- 不要随意混用不同代 Spring Cloud 子项目版本。
- 使用 Spring Cloud BOM 统一依赖版本。
- 已停止维护的 Release Train 不应在新项目中使用。

### 4.2 BOM

BOM 是 Bill of Materials，用于统一管理依赖版本。

Maven 示例：

```xml
<properties>
  <spring-cloud.version>2025.0.0</spring-cloud.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>${spring-cloud.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

价值：

- 统一 Spring Cloud 子项目版本。
- 减少依赖冲突。
- 降低升级风险。
- 和 Spring Boot 版本保持兼容。

## 5. 微服务基础

### 5.1 微服务是什么

微服务是一种架构风格，它把一个大型系统拆分成多个围绕业务能力的小服务。每个服务可以独立开发、测试、部署和扩展。

微服务常见特点：

- 服务独立部署。
- 服务围绕业务能力拆分。
- 服务之间通过网络通信。
- 每个服务可以拥有自己的数据库。
- 服务可以独立扩缩容。
- 故障隔离能力更强。

### 5.2 微服务的优点

优点：

- 团队可以并行开发。
- 服务可以独立发布。
- 不同服务可以按需扩容。
- 单个服务故障影响范围相对可控。
- 技术栈可以局部演进。
- 复杂系统边界更清晰。

### 5.3 微服务的代价

微服务不是银弹。

代价：

- 网络调用增加延迟。
- 分布式事务复杂。
- 服务治理复杂。
- 测试复杂。
- 部署和监控复杂。
- 日志分散。
- 故障排查难。
- 团队协作要求更高。

因此，不建议小项目或边界不清晰的项目过早微服务化。

## 6. 服务注册与发现

### 6.1 为什么需要服务发现

在微服务系统中，一个服务可能有多个实例，实例地址也可能因为扩缩容、重启、发布而变化。

如果调用方写死地址，会带来问题：

- 地址变化需要改配置。
- 无法自动感知新实例。
- 无法剔除故障实例。
- 无法做负载均衡。

服务发现解决的是：

```text
调用方如何找到目标服务当前可用实例
```

### 6.2 服务注册中心

注册中心保存服务实例信息。

常见注册中心：

- Eureka。
- Consul。
- Zookeeper。
- Nacos。
- Kubernetes Service。

基本流程：

1. 服务启动后向注册中心注册自己。
2. 服务定期发送心跳。
3. 注册中心维护服务实例列表。
4. 调用方从注册中心获取目标服务实例。
5. 调用方选择一个实例发起请求。

### 6.3 Eureka

Eureka 来自 Netflix OSS，Spring Cloud Netflix 提供过集成。

核心概念：

- Eureka Server。
- Eureka Client。
- 服务注册。
- 服务续约。
- 服务下线。
- 自我保护机制。

Eureka 适合传统 Spring Cloud 微服务学习和老项目维护。

注意：

- 新项目中很多团队会选择 Nacos、Consul 或 Kubernetes 原生服务发现。
- Spring Cloud Netflix 中很多组件已不再是新项目首选。

### 6.4 Consul

Consul 是 HashiCorp 提供的服务发现和配置工具。

能力：

- 服务注册发现。
- 健康检查。
- Key/Value 配置。
- 多数据中心。

Spring Cloud Consul 可以把 Consul 集成到 Spring Cloud DiscoveryClient 和配置体系中。

### 6.5 Zookeeper

Zookeeper 是分布式协调服务。

能力：

- 服务注册。
- 配置管理。
- 分布式锁。
- 集群协调。

Spring Cloud Zookeeper 提供基于 Zookeeper 的服务发现和配置集成。

### 6.6 Kubernetes 服务发现

在 Kubernetes 中，服务发现通常由 Kubernetes Service、DNS、EndpointSlice 实现。

Spring Cloud Kubernetes 提供：

- DiscoveryClient。
- ConfigMap 集成。
- Secret 集成。
- Kubernetes 原生环境适配。

适合：

- 应用已经部署在 Kubernetes。
- 希望减少额外注册中心。
- 云原生架构。

## 7. Spring Cloud Commons

Spring Cloud Commons 提供 Spring Cloud 中很多通用抽象。

核心能力：

- DiscoveryClient。
- ServiceRegistry。
- LoadBalancer。
- Bootstrap/Context 相关支持。
- 通用注解和接口。

价值：

- 屏蔽不同注册中心差异。
- 为 OpenFeign、Gateway、LoadBalancer 等组件提供统一基础。

例如调用方可以通过服务名访问目标服务，而不是关心服务来自 Eureka、Consul、Zookeeper 还是 Kubernetes。

## 8. 服务间调用

### 8.1 服务调用方式

微服务之间常见调用方式：

- HTTP REST。
- gRPC。
- 消息队列。
- GraphQL。
- RSocket。

Spring Cloud 中最常见的是：

- OpenFeign 声明式 HTTP 调用。
- RestClient/WebClient 结合 LoadBalancer。
- Spring Cloud Stream 做消息通信。

### 8.2 同步调用和异步调用

同步调用：

- 调用方等待响应。
- 编程模型简单。
- 链路耦合更强。
- 下游慢会影响上游。

异步调用：

- 通过消息队列解耦。
- 调用方不直接等待结果。
- 适合削峰和最终一致性。
- 调试和一致性更复杂。

选型建议：

- 查询类、强实时类接口常用同步调用。
- 通知、日志、状态同步、异步处理常用消息。
- 关键链路要考虑超时、重试、熔断和降级。

## 9. Spring Cloud OpenFeign

### 9.1 OpenFeign 是什么

Spring Cloud OpenFeign 是声明式 HTTP 客户端。它允许开发者通过 Java 接口描述远程 HTTP 服务，Spring Cloud 会生成代理对象完成实际调用。

示例：

```java
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUser(@PathVariable("id") Long id);
}
```

使用：

```java
UserResponse user = userClient.getUser(1L);
```

价值：

- 减少手写 HTTP 请求代码。
- 接口语义清晰。
- 与服务发现和负载均衡集成。
- 方便统一配置超时、日志、拦截器、错误处理。

### 9.2 启用 OpenFeign

```java
@SpringBootApplication
@EnableFeignClients
public class OrderApplication {
}
```

依赖：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### 9.3 Feign 配置

常见配置：

- 连接超时。
- 读取超时。
- 日志级别。
- 编码器。
- 解码器。
- 错误解码。
- 请求拦截器。
- 重试策略。

示例：

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          user-service:
            connect-timeout: 2000
            read-timeout: 5000
```

### 9.4 Feign 拦截器

适合传递：

- token。
- traceId。
- 租户 ID。
- 语言环境。
- 灰度标识。

示例：

```java
@Bean
public RequestInterceptor requestInterceptor() {
    return template -> template.header("X-Trace-Id", TraceContext.getTraceId());
}
```

### 9.5 Feign 使用注意事项

注意：

- 必须设置超时。
- 不要无限重试。
- 下游异常要统一处理。
- 大文件上传下载不一定适合 Feign。
- 不要在循环中大量调用远程接口。
- 远程调用要考虑熔断、限流和降级。

## 10. 负载均衡

### 10.1 客户端负载均衡

客户端负载均衡是调用方从服务实例列表中选择一个实例发起请求。

Spring Cloud LoadBalancer 是 Spring Cloud 现在常用的客户端负载均衡抽象。

基本过程：

1. 调用方根据服务名查询实例列表。
2. 负载均衡器选择一个实例。
3. 客户端向该实例发起请求。

### 10.2 常见负载均衡策略

常见策略：

- 轮询。
- 随机。
- 权重。
- 最少连接。
- 基于响应时间。
- 基于区域或机房。

Spring Cloud LoadBalancer 默认常见策略是轮询。

### 10.3 负载均衡关注点

需要关注：

- 实例健康状态。
- 调用超时。
- 重试策略。
- 跨机房调用。
- 灰度实例。
- 版本路由。

负载均衡不是只“分摊流量”，还涉及稳定性和发布治理。

## 11. Spring Cloud Gateway

### 11.1 Gateway 是什么

Spring Cloud Gateway 是基于 Spring Framework 和 Spring Boot 的智能可编程路由网关。

网关是微服务入口层，通常位于客户端和后端服务之间。

常见职责：

- 统一入口。
- 路由转发。
- 鉴权。
- 限流。
- 熔断。
- 日志。
- 跨域。
- 请求改写。
- 响应改写。
- 灰度路由。
- 协议适配。

### 11.2 核心概念

Spring Cloud Gateway 核心概念：

- Route。
- Predicate。
- Filter。

Route：

- 路由规则，决定请求转发到哪里。

Predicate：

- 断言，决定请求是否匹配路由。

Filter：

- 过滤器，用于修改请求、响应或执行附加逻辑。

### 11.3 路由配置

示例：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
```

说明：

- `id` 是路由 ID。
- `uri` 是目标地址。
- `lb://` 表示使用服务发现和负载均衡。
- `Path` 表示路径匹配。
- `StripPrefix` 表示去掉路径前缀。

### 11.4 Predicate

常见 Predicate：

- Path。
- Method。
- Header。
- Query。
- Host。
- Cookie。
- After。
- Before。
- Between。
- Weight。

用途：

- 根据路径路由。
- 根据 HTTP 方法路由。
- 根据请求头做灰度。
- 根据 Host 做多域名路由。
- 根据权重做流量分配。

### 11.5 Filter

常见 Filter：

- AddRequestHeader。
- AddResponseHeader。
- RewritePath。
- StripPrefix。
- RequestRateLimiter。
- CircuitBreaker。
- Retry。

自定义全局过滤器可用于：

- 统一鉴权。
- 日志记录。
- Trace ID。
- 请求耗时。
- 黑白名单。
- 租户识别。

### 11.6 网关限流

限流用于保护后端服务。

常见算法：

- 令牌桶。
- 漏桶。
- 固定窗口。
- 滑动窗口。

Gateway 常结合 Redis 实现分布式限流。

限流维度：

- IP。
- 用户 ID。
- API。
- 租户。
- 应用。

### 11.7 网关安全注意事项

网关应关注：

- 认证。
- 授权。
- CORS。
- 请求体大小限制。
- Header 清洗。
- 敏感路径保护。
- 内部接口隔离。
- 访问日志。

注意：

- 网关鉴权不能替代服务内部关键权限校验。
- 高价值资源应在业务服务中再次校验权限。

## 12. Spring Cloud Config

### 12.1 Config 是什么

Spring Cloud Config 提供集中化外部配置管理。

它通常分为：

- Config Server。
- Config Client。

配置可以存储在：

- Git。
- 文件系统。
- Vault。
- 数据库或其他后端，视扩展而定。

### 12.2 为什么需要配置中心

微服务多了之后，如果每个服务配置分散在本地，会出现：

- 配置不一致。
- 修改配置需要逐个服务处理。
- 无法统一审计。
- 无法版本化管理。
- 环境配置容易混乱。

配置中心的价值：

- 集中管理。
- 多环境隔离。
- 配置版本化。
- 动态刷新。
- 配置审计。

### 12.3 Config Server

Config Server 负责读取远程配置并提供 HTTP 接口。

示例依赖：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

启用：

```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
}
```

### 12.4 Config Client

客户端依赖：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

客户端会从 Config Server 获取配置并加载到 Spring Environment。

### 12.5 配置刷新

配置刷新常见方式：

- Actuator `/actuator/refresh`。
- Spring Cloud Bus 广播刷新事件。
- 重启服务。

注意：

- 不是所有 Bean 都会自动刷新。
- 动态刷新配置要谨慎用于关键配置。
- 数据库连接、线程池等复杂资源的动态变更需要专项设计。

## 13. Spring Cloud Bus

### 13.1 Bus 是什么

Spring Cloud Bus 使用消息代理连接多个服务实例，用于在集群中传播状态变化事件。

常见用途：

- 配置刷新广播。
- 服务实例之间事件通知。

常见消息中间件：

- RabbitMQ。
- Kafka。

### 13.2 配置刷新场景

没有 Bus：

```text
需要逐个调用每个服务实例的 refresh
```

有 Bus：

```text
调用一个服务实例 busrefresh
通过消息总线广播到所有相关实例
```

价值：

- 降低运维成本。
- 保证多实例配置一致更新。

### 13.3 使用注意事项

注意：

- Bus 依赖消息中间件。
- 刷新事件需要权限保护。
- 不要把敏感管理端点暴露到公网。
- 配置刷新要有审计和回滚机制。

## 14. 熔断、限流与降级

### 14.1 为什么需要熔断

微服务调用链中，下游服务异常可能拖垮上游。

例如：

```text
订单服务 -> 用户服务 -> 权限服务
```

如果用户服务变慢，订单服务线程可能被大量阻塞，最终订单服务也不可用。

熔断的目标：

- 快速失败。
- 避免故障扩散。
- 给下游恢复时间。
- 保护系统整体可用性。

### 14.2 Spring Cloud Circuit Breaker

Spring Cloud Circuit Breaker 提供统一熔断抽象，可集成不同实现。

常见实现：

- Resilience4j。

它提供统一 API，让应用不直接绑定具体熔断库。

### 14.3 Resilience4j

Resilience4j 是轻量容错库。

常见能力：

- CircuitBreaker。
- Retry。
- RateLimiter。
- Bulkhead。
- TimeLimiter。

#### CircuitBreaker

熔断器状态：

- CLOSED：正常调用。
- OPEN：熔断打开，快速失败。
- HALF_OPEN：半开，允许少量请求试探恢复。

#### Retry

重试用于处理短暂故障。

注意：

- 只能对幂等操作安全重试。
- 需要设置最大次数。
- 需要退避策略。
- 不要在高并发故障时大量重试放大流量。

#### Bulkhead

舱壁隔离用于限制某类调用占用资源，避免一个下游拖垮全部线程。

适合：

- 按下游服务隔离线程池。
- 按业务类型隔离资源。

#### TimeLimiter

超时限制用于防止请求无限等待。

微服务调用必须设置超时。

### 14.4 降级策略

降级是当系统异常时返回备用结果。

常见降级：

- 返回默认值。
- 返回缓存数据。
- 关闭非核心功能。
- 提示稍后重试。
- 异步补偿。

注意：

- 降级不是简单吞异常。
- 降级结果要符合业务预期。
- 高风险业务不能随意降级。

## 15. Spring Cloud Stream

### 15.1 Stream 是什么

Spring Cloud Stream 是构建消息驱动微服务的框架。

它通过 Binder 抽象屏蔽底层消息中间件差异。

常见 Binder：

- Kafka。
- RabbitMQ。

价值：

- 用统一编程模型处理消息。
- 降低绑定具体 MQ 的耦合。
- 支持生产者和消费者。

### 15.2 消息驱动场景

适合：

- 订单创建事件。
- 支付成功事件。
- 库存变更事件。
- 用户行为日志。
- 数据同步。
- 异步通知。

### 15.3 函数式模型

Spring Cloud Stream 支持函数式编程模型。

示例概念：

```java
@Bean
public Consumer<OrderCreatedEvent> handleOrderCreated() {
    return event -> {
        // handle event
    };
}
```

### 15.4 消息可靠性

必须关注：

- 消息是否发送成功。
- 消息是否被消费。
- 消费失败如何重试。
- 是否需要死信队列。
- 消息是否可能重复。
- 消费是否幂等。
- 是否需要顺序消费。

消息系统的核心原则：

```text
默认假设消息可能重复，消费者必须幂等
```

## 16. Spring Cloud Contract

### 16.1 Contract 是什么

Spring Cloud Contract 用于契约测试。

它解决的问题：

- 服务提供方和消费方接口约定不一致。
- 提供方改接口导致消费方运行时失败。
- 集成测试依赖真实服务，成本高。

### 16.2 消费者驱动契约

消费者驱动契约强调由消费方定义自己需要的接口行为，提供方通过测试保证兼容。

价值：

- 提前发现接口不兼容。
- 降低服务间协作风险。
- 提升微服务独立发布信心。

### 16.3 适用场景

适合：

- 多团队协作。
- 接口频繁演进。
- 微服务数量多。
- 对兼容性要求高。

注意：

- 契约维护需要纪律。
- 不应替代所有集成测试。
- 契约应聚焦关键交互。

## 17. Spring Cloud Kubernetes

### 17.1 Kubernetes 环境下的 Spring Cloud

如果服务运行在 Kubernetes 中，很多能力可以使用 Kubernetes 原生机制：

- 服务发现。
- 配置管理。
- Secret 管理。
- 负载均衡。
- 健康检查。
- 滚动发布。

Spring Cloud Kubernetes 让 Spring 应用更好集成 Kubernetes。

### 17.2 ConfigMap 和 Secret

ConfigMap 用于非敏感配置。

Secret 用于敏感配置。

Spring Cloud Kubernetes 可以把这些配置加载到应用环境中。

注意：

- Secret 不是绝对安全，只是 Kubernetes 中的敏感配置对象。
- 敏感数据仍需要访问控制和加密管理。

### 17.3 Kubernetes 服务发现

通过 Kubernetes Service 和 DNS，服务可以使用服务名访问。

Spring Cloud Kubernetes 提供 DiscoveryClient 支持，让 Spring Cloud 组件可以使用 Kubernetes 服务发现机制。

## 18. Spring Cloud Vault

### 18.1 Vault 是什么

Spring Cloud Vault 集成 HashiCorp Vault，用于管理敏感配置。

适合管理：

- 数据库密码。
- API Key。
- Token。
- 证书。
- 动态凭据。

### 18.2 为什么需要 Vault

普通配置中心不适合保存高敏感密钥。

Vault 提供：

- 密钥集中管理。
- 动态凭据。
- 租约。
- 自动轮换。
- 审计。
- 加密存储。

### 18.3 使用注意事项

注意：

- 应用需要安全认证访问 Vault。
- 密钥权限应最小化。
- 密钥轮换要和应用刷新机制配合。
- Vault 本身也需要高可用部署。

## 19. Spring Cloud Function 与 Task

### 19.1 Spring Cloud Function

Spring Cloud Function 让业务逻辑以函数形式编写，并支持在不同运行环境中复用。

适合：

- Serverless。
- 事件处理。
- 消息处理。
- 独立函数逻辑。

核心接口：

- Supplier。
- Function。
- Consumer。

### 19.2 Spring Cloud Task

Spring Cloud Task 用于短生命周期任务。

适合：

- 批处理。
- 一次性任务。
- 数据导入导出。
- 定时触发任务。
- 和 Spring Batch 集成。

与常驻服务不同，Task 执行完成后会退出。

## 20. Spring Cloud Alibaba

### 20.1 Spring Cloud Alibaba 是什么

Spring Cloud Alibaba 是 Spring Cloud 在阿里中间件生态中的扩展。

常见组件：

- Nacos。
- Sentinel。
- Seata。
- RocketMQ。

国内项目中经常使用。

### 20.2 Nacos

Nacos 提供：

- 服务注册发现。
- 配置中心。

特点：

- 控制台友好。
- 国内生态广。
- 支持动态配置。
- 支持命名空间、分组。

### 20.3 Sentinel

Sentinel 提供：

- 流量控制。
- 熔断降级。
- 系统保护。
- 热点参数限流。

适合：

- 高并发接口保护。
- 秒杀。
- 网关限流。
- 服务保护。

### 20.4 Seata

Seata 用于分布式事务。

常见模式：

- AT。
- TCC。
- Saga。
- XA。

注意：

- 分布式事务成本高。
- 应优先通过业务设计降低强一致需求。
- 使用前需要充分压测和评估。

## 21. 服务网关设计

### 21.1 网关在系统中的位置

典型链路：

```text
Client -> API Gateway -> Microservices
```

网关是系统入口，但不是所有业务逻辑的集中地。

网关适合：

- 通用鉴权。
- 路由。
- 限流。
- 黑白名单。
- 日志。
- CORS。
- 协议转换。

不适合：

- 大量业务规则。
- 复杂业务编排。
- 深度耦合具体服务内部逻辑。

### 21.2 网关和 BFF

API Gateway 偏基础设施入口。

BFF 偏面向前端体验的数据聚合和裁剪。

区别：

- 网关关注流量治理。
- BFF 关注终端适配和接口聚合。

很多系统会同时存在：

```text
Client -> Gateway -> BFF -> Microservices
```

## 22. 配置中心设计

### 22.1 配置分类

配置可以分为：

- 应用配置。
- 环境配置。
- 业务开关。
- 灰度配置。
- 密钥配置。
- 中间件配置。

不同配置管理方式不同。

### 22.2 配置中心注意事项

注意：

- 配置要有版本记录。
- 修改配置要有权限控制。
- 高风险配置要审批。
- 配置刷新要可回滚。
- 配置变更要有通知和审计。
- 密钥不应随普通配置裸露。

### 22.3 动态配置风险

动态配置很强大，但也危险。

风险：

- 修改错误立即影响全局。
- 配置类型错误导致服务异常。
- 多实例刷新不一致。
- 配置和代码版本不匹配。

建议：

- 配置校验。
- 灰度发布配置。
- 变更审计。
- 回滚机制。

## 23. 服务调用治理

### 23.1 超时

所有远程调用必须设置超时。

需要设置：

- 连接超时。
- 读取超时。
- 总超时。

没有超时会导致线程长期阻塞，最终拖垮服务。

### 23.2 重试

重试适合短暂故障。

注意：

- 只对幂等操作重试。
- 设置最大次数。
- 使用指数退避。
- 避免雪崩时放大流量。

### 23.3 幂等

微服务中必须重视幂等。

场景：

- Feign 重试。
- MQ 重复投递。
- 支付回调。
- 订单提交。
- 定时任务补偿。

实现：

- 唯一业务号。
- 数据库唯一索引。
- Redis 去重。
- 状态机。
- 幂等表。

### 23.4 隔离

隔离用于防止一个下游影响整个系统。

方式：

- 线程池隔离。
- 信号量隔离。
- 连接池隔离。
- 服务隔离。
- 数据库隔离。

## 24. 分布式链路追踪与可观测性

### 24.1 为什么需要链路追踪

微服务请求可能经过多个服务。

没有链路追踪时：

- 不知道请求经过哪些服务。
- 不知道哪个服务慢。
- 不知道错误发生在哪个节点。
- 日志难以串联。

### 24.2 Trace ID

每次请求生成 traceId，并在服务间传递。

日志中打印 traceId，可以串联完整请求链路。

### 24.3 Micrometer 和 OpenTelemetry

现代 Spring 生态常用 Micrometer Observation、Tracing 和 OpenTelemetry 进行指标与链路追踪集成。

可接入：

- Prometheus。
- Grafana。
- Zipkin。
- Jaeger。
- Tempo。
- SkyWalking。

### 24.4 监控指标

微服务需要监控：

- QPS。
- 错误率。
- P95/P99 延迟。
- 熔断次数。
- 限流次数。
- 网关请求量。
- Feign 调用耗时。
- 线程池使用率。
- 数据库连接池。
- MQ 积压。
- JVM GC。

## 25. 微服务安全

### 25.1 认证授权

常见方案：

- Session。
- JWT。
- OAuth2。
- OIDC。
- API Key。

微服务中常见：

- 网关统一认证。
- 业务服务做权限校验。
- Token 透传。
- 服务间调用身份认证。

### 25.2 OAuth2 Resource Server

业务服务可以作为资源服务器，校验访问令牌。

适合：

- 统一认证中心。
- 多服务鉴权。
- 第三方授权。

### 25.3 服务间安全

服务间调用也需要安全。

方式：

- 内网隔离。
- mTLS。
- 服务账号。
- API Key。
- JWT。
- 网格安全策略。

### 25.4 网关鉴权误区

误区：

```text
网关做了鉴权，后端服务就不用校验
```

实际建议：

- 网关做通用认证和粗粒度权限。
- 服务内部做业务权限和数据权限。
- 高风险接口必须二次校验。

## 26. 分布式事务与一致性

### 26.1 为什么复杂

单体应用中，一个数据库事务可以覆盖多个表操作。

微服务中，每个服务可能拥有自己的数据库，跨服务事务变成分布式事务。

问题：

- 网络可能失败。
- 服务可能宕机。
- 部分操作成功部分失败。
- 回滚成本高。

### 26.2 常见方案

方案：

- 2PC。
- TCC。
- Saga。
- 本地消息表。
- 事务消息。
- 最大努力通知。
- 对账补偿。

### 26.3 最终一致性

很多业务可以接受短暂不一致，最终通过消息、补偿、重试达到一致。

适合：

- 订单通知。
- 积分发放。
- 优惠券发放。
- 数据同步。

不适合随意弱化：

- 资金入账。
- 支付扣款。
- 核心库存。

### 26.4 设计建议

建议：

- 优先避免跨服务强事务。
- 服务边界围绕业务一致性设计。
- 使用状态机。
- 所有消息消费幂等。
- 关键链路有补偿和对账。

## 27. 灰度发布与流量治理

### 27.1 灰度发布

灰度发布是让新版本只接收部分流量。

维度：

- 用户 ID。
- 租户。
- 地区。
- 请求头。
- Cookie。
- 权重比例。

### 27.2 版本路由

网关可以根据请求头路由到不同版本服务。

示例：

```text
X-Version: v2 -> user-service-v2
```

### 27.3 金丝雀发布

金丝雀发布先让少量真实流量进入新版本，观察指标正常后逐步放量。

观察指标：

- 错误率。
- P95/P99。
- CPU。
- 内存。
- 业务转化。
- 日志异常。

### 27.4 回滚

回滚要求：

- 镜像版本可追溯。
- 配置可回滚。
- 数据结构兼容。
- 网关路由可快速切回。
- 监控能确认恢复。

## 28. 常见微服务架构模式

### 28.1 API Gateway

统一入口，负责路由和流量治理。

### 28.2 Config Server

集中配置管理。

### 28.3 Service Registry

服务注册与发现。

### 28.4 Circuit Breaker

防止故障扩散。

### 28.5 Event Driven

通过事件解耦服务。

### 28.6 CQRS

读写分离模型，适合复杂查询和命令处理分离。

### 28.7 Saga

长事务拆成多个本地事务，通过补偿保证最终一致性。

## 29. 常见问题排查

### 29.1 服务注册不上

可能原因：

- 注册中心地址错误。
- 服务名配置错误。
- 网络不通。
- 健康检查失败。
- 版本兼容问题。
- 权限认证失败。

### 29.2 Feign 调用失败

排查：

- 服务名是否正确。
- 服务是否注册。
- 路径是否正确。
- 参数绑定是否一致。
- 超时是否过短。
- 下游是否报错。
- CORS 与 Feign 无关，不要混淆。

### 29.3 Gateway 路由不生效

排查：

- Predicate 是否匹配。
- Path 是否正确。
- StripPrefix 是否配置正确。
- uri 是否正确。
- 服务是否可发现。
- Filter 是否改写了路径。

### 29.4 配置不刷新

可能原因：

- 未暴露 refresh 端点。
- Bean 不支持刷新。
- Bus 消息未发送。
- 消息中间件连接失败。
- 配置文件分支或 profile 错误。

### 29.5 熔断频繁打开

排查：

- 下游是否真的慢。
- 超时配置是否过短。
- 错误率阈值是否过低。
- 线程池是否满。
- 数据库是否慢。
- 是否存在重试风暴。

## 30. Spring Cloud 最佳实践

### 30.1 版本统一

使用 Spring Cloud BOM，不要手动混搭版本。

### 30.2 超时必须配置

所有远程调用都要设置超时。

### 30.3 重试必须谨慎

只对幂等接口重试。

### 30.4 消费者必须幂等

消息消费、回调、重试场景都需要幂等设计。

### 30.5 网关不写复杂业务

网关处理通用流量逻辑，不应变成业务服务。

### 30.6 配置变更要审计

配置中心必须有权限、审计和回滚。

### 30.7 可观测性先行

微服务没有日志、指标、链路追踪，很难稳定运维。

### 30.8 服务拆分要谨慎

不要为了微服务而微服务。

服务拆分应基于：

- 业务边界。
- 团队边界。
- 数据一致性边界。
- 发布频率。
- 扩展需求。

## 31. 学习路线建议

推荐学习顺序：

1. Spring Boot。
2. 微服务基础概念。
3. Spring Cloud 版本和 BOM。
4. 服务注册与发现。
5. Spring Cloud OpenFeign。
6. Spring Cloud LoadBalancer。
7. Spring Cloud Gateway。
8. Spring Cloud Config。
9. Spring Cloud Bus。
10. Spring Cloud Circuit Breaker 和 Resilience4j。
11. Spring Cloud Stream。
12. 链路追踪和监控。
13. Spring Cloud Kubernetes。
14. Spring Cloud Alibaba。
15. 分布式事务、幂等和最终一致性。
16. 灰度发布和流量治理。
17. 完整微服务项目实战。

## 32. 推荐实践项目

### 32.1 用户中心微服务

包含：

- 用户注册。
- 用户登录。
- 用户查询。
- 角色权限。
- JWT。

训练：

- Spring Boot。
- Spring Security。
- OpenFeign。
- Gateway 鉴权。

### 32.2 订单微服务系统

包含：

- 用户服务。
- 商品服务。
- 库存服务。
- 订单服务。
- 支付服务。
- 通知服务。

训练：

- 服务发现。
- Feign 调用。
- MQ 异步。
- 幂等。
- 分布式事务。
- 熔断降级。

### 32.3 配置中心与灰度系统

包含：

- Config Server。
- 配置刷新。
- Bus。
- 网关灰度路由。
- Feature Flag。

训练：

- 配置治理。
- 动态配置。
- 灰度发布。
- 回滚。

### 32.4 微服务监控平台

包含：

- Actuator。
- Prometheus。
- Grafana。
- OpenTelemetry。
- Trace ID。
- 日志聚合。

训练：

- 可观测性。
- 性能指标。
- 链路排查。

## 33. 能力自检清单

你应该能够回答：

- Spring Cloud 和 Spring Boot 的关系是什么？
- Spring Cloud Release Train 是什么？
- 为什么要使用 Spring Cloud BOM？
- 服务注册与发现解决什么问题？
- Eureka、Consul、Nacos、Kubernetes 服务发现如何选择？
- OpenFeign 的原理和适用场景是什么？
- Feign 调用为什么必须设置超时？
- Spring Cloud LoadBalancer 做什么？
- Gateway 的 Route、Predicate、Filter 分别是什么？
- Gateway 如何做限流和鉴权？
- Config Server 解决什么问题？
- Spring Cloud Bus 如何广播配置刷新？
- 熔断、限流、降级有什么区别？
- Resilience4j 有哪些核心组件？
- Spring Cloud Stream 的 Binder 是什么？
- 消息重复消费如何处理？
- 微服务中如何传递 traceId？
- 网关鉴权后，业务服务还要不要做权限校验？
- 分布式事务有哪些方案？
- 什么时候不应该拆微服务？

## 34. 官方资料入口

- Spring Cloud 官方项目页  
  https://spring.io/projects/spring-cloud/

- Spring Cloud Reference  
  https://docs.spring.io/spring-cloud/docs/current/reference/html/

- Spring Cloud Gateway  
  https://docs.spring.io/spring-cloud-gateway/reference/

- Spring Cloud OpenFeign  
  https://docs.spring.io/spring-cloud-openfeign/reference/

- Spring Cloud Config  
  https://docs.spring.io/spring-cloud-config/reference/

- Spring Cloud Circuit Breaker  
  https://docs.spring.io/spring-cloud-circuitbreaker/reference/

- Spring Cloud Stream  
  https://docs.spring.io/spring-cloud-stream/reference/

- Spring Cloud Kubernetes  
  https://docs.spring.io/spring-cloud-kubernetes/reference/

## 35. 总结

Spring Cloud 是构建微服务和分布式系统的重要工具集。它提供的不是某一个单点能力，而是一整套围绕服务治理的解决方案：服务发现、配置中心、远程调用、负载均衡、网关、熔断、消息驱动、配置刷新、契约测试、云原生集成和可观测性。

学习 Spring Cloud 不能只停留在组件使用层面。真正重要的是理解每个组件背后的分布式问题：服务地址变化、网络不可靠、下游故障、配置不一致、消息重复、链路难追踪、事务难保证、发布有风险。掌握这些问题的本质，才能在实际项目中设计稳定、可维护、可演进的微服务系统。
