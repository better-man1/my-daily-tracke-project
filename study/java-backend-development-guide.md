# Java 后台开发技术体系学习指南

## 1. 学习目标

Java 后台开发不是只会写接口，也不是只会使用 Spring Boot。真正系统的后台开发能力包括：语言基础、面向对象、集合、并发、JVM、数据库、缓存、消息队列、Web 协议、Spring 生态、系统设计、测试、部署、监控、安全和分布式架构。

如果目标是成为优秀的 Java 后台工程师，需要具备以下能力：

- 能写出清晰、稳定、可维护的 Java 代码。
- 能理解 JVM、线程、内存、GC 等底层机制。
- 能使用 Spring Boot 构建可靠后端服务。
- 能设计 RESTful API 和统一接口规范。
- 能设计数据库表、索引、事务和查询优化方案。
- 能使用 Redis、消息队列等中间件解决高并发和异步问题。
- 能处理认证、授权、安全、限流、幂等和数据一致性。
- 能写单元测试、集成测试和接口测试。
- 能部署服务、查看日志、定位线上问题。
- 能理解微服务、分布式事务、服务治理和系统架构演进。

## 2. 总体知识地图

Java 后台开发需要掌握的知识体系可以分为：

1. Java 语言基础。
2. 面向对象和设计原则。
3. Java 集合框架。
4. 异常、泛型、注解、反射。
5. Java I/O 与 NIO。
6. Java 并发编程。
7. JVM 原理。
8. Maven、Gradle 和工程管理。
9. Git、Linux 和常用开发工具。
10. HTTP、Web、RESTful API。
11. Spring Framework。
12. Spring Boot。
13. Spring MVC。
14. MyBatis、JPA 和数据访问。
15. MySQL 和数据库设计。
16. Redis 缓存。
17. 消息队列。
18. Spring Security 和认证授权。
19. 测试体系。
20. 日志、监控和可观测性。
21. Docker、Kubernetes 和 DevOps。
22. 微服务和 Spring Cloud。
23. 分布式系统。
24. 性能优化和线上排障。
25. 架构设计、DDD 和项目实践。

这些知识是层层递进的。学习顺序建议先打牢 Java 和数据库基础，再学习 Spring Boot 和数据访问，然后补齐缓存、消息队列、测试、部署、监控，最后进入分布式和架构设计。

## 3. Java 语言基础

### 3.1 基本语法

必须掌握：

- 变量。
- 数据类型。
- 运算符。
- 条件语句。
- 循环语句。
- 方法。
- 类和对象。
- 包和访问控制。

Java 是强类型语言，每个变量都有明确类型。

示例：

```java
int age = 18;
String name = "Alice";
boolean active = true;
```

后台开发中，基础语法看似简单，但它决定了你能否写出清晰、可靠的业务逻辑。

### 3.2 基本数据类型和包装类型

Java 基本数据类型：

- `byte`
- `short`
- `int`
- `long`
- `float`
- `double`
- `char`
- `boolean`

对应包装类型：

- `Byte`
- `Short`
- `Integer`
- `Long`
- `Float`
- `Double`
- `Character`
- `Boolean`

需要理解：

- 基本类型存储值。
- 包装类型是对象。
- 包装类型可能为 `null`。
- 自动装箱和拆箱可能带来空指针和性能问题。

示例：

```java
Integer count = null;
int value = count; // 可能抛出 NullPointerException
```

后台开发建议：

- DTO 和数据库映射对象中常用包装类型，因为字段可能为空。
- 计算逻辑中常用基本类型，避免不必要装箱。
- 金额不要用 `double`，应使用 `BigDecimal`。

### 3.3 String

`String` 是 Java 后台开发中最常用类型之一。

重点掌握：

- String 不可变。
- 字符串常量池。
- `equals` 和 `==` 的区别。
- StringBuilder。
- StringBuffer。

示例：

```java
String a = "hello";
String b = new String("hello");

System.out.println(a == b);       // false
System.out.println(a.equals(b));  // true
```

说明：

- `==` 比较对象引用。
- `equals` 比较字符串内容。

字符串拼接建议：

- 少量拼接可以直接使用 `+`。
- 循环中大量拼接使用 `StringBuilder`。

### 3.4 BigDecimal

金额计算必须使用 `BigDecimal`，不要使用 `float` 或 `double`。

示例：

```java
BigDecimal price = new BigDecimal("19.99");
BigDecimal count = new BigDecimal("3");
BigDecimal total = price.multiply(count);
```

注意：

- 使用字符串构造 BigDecimal。
- 除法要指定精度和舍入方式。

```java
BigDecimal result = amount.divide(rate, 2, RoundingMode.HALF_UP);
```

后台系统中，订单、支付、结算、财务相关逻辑都必须严谨处理金额精度。

## 4. 面向对象编程

### 4.1 类和对象

类是对象的模板，对象是类的实例。

```java
public class User {
    private Long id;
    private String username;
}
```

后台开发中，类通常表示：

- 领域对象。
- 数据库实体。
- DTO。
- Service。
- Controller。
- 配置类。
- 工具类。

### 4.2 封装

封装是隐藏内部实现，只暴露必要接口。

价值：

- 控制对象状态。
- 减少外部误用。
- 降低耦合。
- 提升可维护性。

示例：

```java
public class Account {
    private BigDecimal balance;

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.balance = this.balance.subtract(amount);
    }
}
```

不要让业务对象只是 getter/setter 容器。复杂业务规则应尽量靠近对象本身。

### 4.3 继承

继承用于表达 “is-a” 关系。

```java
public class AdminUser extends User {
}
```

注意：

- 继承会增加耦合。
- 不要为了复用代码滥用继承。
- 组合通常比继承更灵活。

### 4.4 多态

多态是同一接口在不同实现中表现不同行为。

```java
public interface PaymentService {
    void pay(Order order);
}

public class AliPayService implements PaymentService {
    public void pay(Order order) {}
}

public class WeChatPayService implements PaymentService {
    public void pay(Order order) {}
}
```

价值：

- 面向接口编程。
- 方便扩展。
- 降低调用方和具体实现的耦合。

### 4.5 接口和抽象类

接口适合定义能力契约。

抽象类适合抽取公共状态和公共逻辑。

选择建议：

- 只定义行为规范，用接口。
- 多个子类共享通用逻辑，用抽象类。
- Java 支持多接口实现，但只支持单继承。

## 5. 设计原则和设计模式

### 5.1 SOLID 原则

SOLID 包括：

- 单一职责原则。
- 开闭原则。
- 里氏替换原则。
- 接口隔离原则。
- 依赖倒置原则。

后台开发中最常用的是：

- 单一职责：Controller 不写业务，Service 不写 SQL。
- 开闭原则：新增支付方式时扩展实现，而不是大量修改旧代码。
- 依赖倒置：面向接口编程，而不是依赖具体类。

### 5.2 常用设计模式

必须掌握：

- 单例模式。
- 工厂模式。
- 策略模式。
- 模板方法模式。
- 责任链模式。
- 代理模式。
- 观察者模式。
- 建造者模式。
- 适配器模式。

### 5.3 策略模式

适合处理多种业务算法或规则。

示例场景：

- 多种支付方式。
- 多种优惠计算。
- 多种导出格式。
- 多种消息发送渠道。

核心思想：

```text
调用方依赖统一接口
不同策略实现不同逻辑
运行时选择具体策略
```

### 5.4 责任链模式

适合多个处理器按顺序处理请求。

场景：

- 参数校验链。
- 风控规则链。
- 审批流程。
- 网关过滤器。
- 登录认证过滤器。

Spring Security 的过滤器链就是典型责任链思想。

## 6. Java 集合框架

### 6.1 List

常见实现：

- `ArrayList`
- `LinkedList`

`ArrayList`：

- 基于数组。
- 随机访问快。
- 尾部添加快。
- 中间插入删除成本较高。

`LinkedList`：

- 基于链表。
- 插入删除某些场景更灵活。
- 随机访问慢。

实际开发中，绝大多数场景使用 `ArrayList`。

### 6.2 Set

常见实现：

- `HashSet`
- `LinkedHashSet`
- `TreeSet`

用途：

- 去重。
- 判断是否存在。
- 存储不重复元素。

`HashSet` 基于哈希表，不保证顺序。

`LinkedHashSet` 保持插入顺序。

`TreeSet` 支持排序。

### 6.3 Map

常见实现：

- `HashMap`
- `LinkedHashMap`
- `TreeMap`
- `ConcurrentHashMap`

`HashMap` 是最常用键值结构。

重点掌握：

- 哈希函数。
- 哈希冲突。
- 扩容。
- 负载因子。
- 红黑树优化。
- `equals` 和 `hashCode`。

### 6.4 equals 和 hashCode

如果对象作为 HashMap 的 key 或放入 HashSet，需要正确实现 `equals` 和 `hashCode`。

规则：

- equals 相等的对象，hashCode 必须相等。
- hashCode 相等的对象，equals 不一定相等。

错误实现可能导致：

- Set 去重失败。
- Map 查找失败。
- 缓存命中异常。

### 6.5 Stream API

Stream 用于集合数据处理。

示例：

```java
List<String> names = users.stream()
        .filter(User::isActive)
        .map(User::getName)
        .toList();
```

常用操作：

- `filter`
- `map`
- `flatMap`
- `sorted`
- `distinct`
- `collect`
- `groupingBy`
- `joining`

注意：

- Stream 提升可读性，但不要写过长链式调用。
- 大数据量处理要关注性能。
- 并行流不要随意使用。

## 7. 异常处理

### 7.1 异常体系

Java 异常体系：

```text
Throwable
├─ Error
└─ Exception
   ├─ Checked Exception
   └─ RuntimeException
```

`Error` 通常表示严重 JVM 问题，不应业务捕获。

`Exception` 表示程序异常。

`RuntimeException` 是运行时异常。

### 7.2 Checked Exception 和 RuntimeException

Checked Exception：

- 编译器强制处理。
- 如 IOException。

RuntimeException：

- 编译器不强制处理。
- 如 NullPointerException、IllegalArgumentException。

后台业务异常通常继承 RuntimeException。

### 7.3 业务异常

示例：

```java
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
```

建议：

- 业务异常带错误码。
- 统一异常处理。
- 不直接返回堆栈给前端。
- 日志中保留必要上下文。

### 7.4 异常处理原则

原则：

- 能处理就处理，不能处理就向上抛。
- 不要吞异常。
- 不要用异常控制正常流程。
- 捕获异常时保留原始异常。
- 对外返回友好信息，对内记录详细日志。

错误示例：

```java
try {
    doSomething();
} catch (Exception e) {
}
```

这会让问题消失在日志之外，线上排查非常困难。

## 8. 泛型、注解和反射

### 8.1 泛型

泛型让类型在编译期更安全。

示例：

```java
List<String> names = new ArrayList<>();
```

自定义泛型：

```java
public class ApiResponse<T> {
    private T data;
}
```

常见场景：

- 统一响应。
- 分页结果。
- Repository 抽象。
- 工具方法。

### 8.2 注解

注解用于给代码添加元信息。

常见注解：

- `@Override`
- `@Deprecated`
- `@SpringBootApplication`
- `@RestController`
- `@Service`
- `@Repository`
- `@Autowired`
- `@Transactional`

Spring 大量使用注解实现配置、扫描、代理、事务和 Web 映射。

### 8.3 反射

反射允许程序在运行时获取类、方法、字段等信息。

用途：

- 框架对象创建。
- 依赖注入。
- 注解扫描。
- ORM 映射。
- JSON 序列化。

注意：

- 反射性能低于直接调用。
- 反射破坏封装。
- GraalVM Native Image 中反射需要额外配置。

## 9. Java I/O 与 NIO

### 9.1 I/O

Java I/O 用于文件和网络数据读写。

常见类：

- InputStream。
- OutputStream。
- Reader。
- Writer。
- File。

字节流适合二进制数据。

字符流适合文本数据。

### 9.2 NIO

NIO 提供更高性能的 I/O 能力。

核心概念：

- Buffer。
- Channel。
- Selector。

NIO 常用于：

- 网络框架。
- 高并发服务器。
- Netty。

### 9.3 Netty

Netty 是高性能网络编程框架。

常见用途：

- RPC 框架。
- 网关。
- 长连接服务。
- IM 服务。
- 中间件。

普通业务开发不一定直接写 Netty，但理解它有助于理解高性能网络框架。

## 10. Java 并发编程

### 10.1 线程

线程是 Java 并发执行的基础。

创建方式：

- 继承 Thread。
- 实现 Runnable。
- 实现 Callable。
- 使用线程池。

生产环境不建议频繁手动 new Thread，应使用线程池统一管理。

### 10.2 synchronized

`synchronized` 用于加锁，保证同一时刻只有一个线程进入临界区。

示例：

```java
public synchronized void increment() {
    count++;
}
```

需要理解：

- 对象锁。
- 类锁。
- 锁粒度。
- 锁竞争。
- 死锁。

### 10.3 volatile

`volatile` 保证可见性和一定的有序性，但不保证复合操作原子性。

适合：

- 状态标记。
- 开关变量。

不适合：

- 自增计数。

```java
volatile boolean running = true;
```

### 10.4 JUC

JUC 是 `java.util.concurrent` 包。

重点掌握：

- ThreadPoolExecutor。
- ConcurrentHashMap。
- CountDownLatch。
- CyclicBarrier。
- Semaphore。
- ReentrantLock。
- AtomicInteger。
- CompletableFuture。
- BlockingQueue。

### 10.5 线程池

线程池核心参数：

- corePoolSize。
- maximumPoolSize。
- keepAliveTime。
- workQueue。
- threadFactory。
- rejectedExecutionHandler。

建议：

- 不使用无界队列堆积任务。
- 不同业务使用不同线程池。
- 设置线程名称，方便排查。
- 监控线程池队列和活跃线程数。

### 10.6 CompletableFuture

用于异步编排。

示例：

```java
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> getUser(id), executor);
CompletableFuture<List<Order>> orderFuture = CompletableFuture.supplyAsync(() -> getOrders(id), executor);

CompletableFuture.allOf(userFuture, orderFuture).join();
```

适合：

- 并发调用多个接口。
- 异步任务编排。
- 提升接口响应速度。

注意：

- 使用自定义线程池。
- 处理异常。
- 控制超时。

## 11. JVM 原理

### 11.1 JVM 内存结构

重点区域：

- 程序计数器。
- Java 虚拟机栈。
- 本地方法栈。
- 堆。
- 方法区。
- 直接内存。

堆用于存放对象，是 GC 主要管理区域。

栈用于方法调用和局部变量。

### 11.2 类加载机制

类加载过程：

1. 加载。
2. 验证。
3. 准备。
4. 解析。
5. 初始化。

类加载器：

- Bootstrap ClassLoader。
- Platform ClassLoader。
- Application ClassLoader。
- 自定义 ClassLoader。

需要理解双亲委派模型。

### 11.3 GC

GC 是垃圾回收机制。

重点掌握：

- 对象可达性分析。
- GC Roots。
- 新生代。
- 老年代。
- Minor GC。
- Major GC。
- Full GC。

常见收集器：

- G1。
- ZGC。
- Shenandoah。

实际排查中关注：

- GC 频率。
- GC 暂停时间。
- Full GC 原因。
- 堆内存增长趋势。

### 11.4 JVM 调优

常见参数：

```bash
-Xms512m
-Xmx512m
-XX:+UseG1GC
```

调优原则：

- 先监控再调优。
- 不要盲目修改参数。
- 找到内存、GC、线程、CPU 的真实瓶颈。

常用工具：

- jps。
- jstack。
- jmap。
- jcmd。
- VisualVM。
- JFR。
- Arthas。

## 12. Maven 与 Gradle

### 12.1 Maven

Maven 是 Java 项目构建和依赖管理工具。

核心文件：

```text
pom.xml
```

常见命令：

```bash
mvn clean
mvn test
mvn package
mvn install
```

核心概念：

- groupId。
- artifactId。
- version。
- dependency。
- plugin。
- lifecycle。
- profile。

### 12.2 Gradle

Gradle 是另一种构建工具。

特点：

- 配置灵活。
- 增量构建能力强。
- Kotlin DSL 支持好。

常见命令：

```bash
gradle build
gradle test
```

### 12.3 依赖管理

需要掌握：

- 依赖传递。
- 依赖冲突。
- 版本管理。
- scope。
- dependencyManagement。
- BOM。

Spring Boot 通常通过 parent 或 BOM 管理依赖版本。

## 13. Git、Linux 和工具

### 13.1 Git

必须掌握：

- clone。
- add。
- commit。
- branch。
- merge。
- rebase。
- cherry-pick。
- revert。
- tag。
- stash。
- conflict resolution。

后台团队协作中，Git 是基础能力。

### 13.2 Linux

必须掌握：

- 文件操作。
- 权限。
- 进程。
- 端口。
- 日志查看。
- 磁盘查看。
- 网络排查。

常用命令：

```bash
ls
cd
cat
tail
grep
ps
top
df
du
netstat
ss
lsof
curl
```

### 13.3 开发工具

常见工具：

- IntelliJ IDEA。
- Postman。
- Apifox。
- Docker Desktop。
- DBeaver。
- RedisInsight。
- Arthas。
- VisualVM。

## 14. HTTP 与 Web 基础

### 14.1 HTTP

必须掌握：

- 请求方法。
- 状态码。
- Header。
- Cookie。
- Session。
- HTTPS。
- CORS。
- 缓存。

常见状态码：

- 200。
- 201。
- 204。
- 400。
- 401。
- 403。
- 404。
- 409。
- 429。
- 500。
- 502。
- 504。

### 14.2 RESTful API

RESTful 设计示例：

```text
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

建议：

- 使用资源名。
- 使用 HTTP 方法表达动作。
- 统一分页格式。
- 统一错误格式。
- 明确幂等性。

### 14.3 API 文档

常见工具：

- OpenAPI。
- Swagger。
- springdoc-openapi。
- Apifox。

接口文档应包含：

- URL。
- 方法。
- 请求参数。
- 响应结构。
- 错误码。
- 示例。

## 15. Spring Framework

### 15.1 Spring 核心

必须掌握：

- IoC。
- DI。
- Bean。
- ApplicationContext。
- Bean 生命周期。
- AOP。
- 事务。
- 事件机制。

### 15.2 AOP

AOP 是面向切面编程。

适合：

- 日志。
- 事务。
- 权限。
- 监控。
- 审计。

核心概念：

- Aspect。
- Join Point。
- Pointcut。
- Advice。
- Proxy。

Spring 事务就是基于 AOP 代理实现的。

### 15.3 Bean 生命周期

大致过程：

1. 实例化。
2. 属性注入。
3. Aware 回调。
4. BeanPostProcessor 前置处理。
5. 初始化。
6. BeanPostProcessor 后置处理。
7. 使用 Bean。
8. 销毁。

理解 Bean 生命周期有助于排查自动配置、代理、循环依赖和初始化问题。

## 16. Spring Boot

### 16.1 核心能力

必须掌握：

- 自动配置。
- Starter。
- 内嵌服务器。
- 外部化配置。
- Profile。
- Actuator。
- SpringApplication。
- 配置属性绑定。

### 16.2 @SpringBootApplication

组合了：

- `@SpringBootConfiguration`
- `@EnableAutoConfiguration`
- `@ComponentScan`

启动类应放在根包下，方便扫描子包组件。

### 16.3 自动配置

自动配置依赖条件注解：

- `@ConditionalOnClass`
- `@ConditionalOnBean`
- `@ConditionalOnMissingBean`
- `@ConditionalOnProperty`
- `@ConditionalOnWebApplication`

高级要求：

- 能看自动配置报告。
- 能覆盖默认 Bean。
- 能排除不需要的自动配置。

## 17. Spring MVC

### 17.1 Controller

常见注解：

- `@RestController`
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@RequestBody`
- `@PathVariable`
- `@RequestParam`

Controller 只负责：

- 接收参数。
- 参数校验。
- 调用 Service。
- 返回结果。

不要在 Controller 中写复杂业务逻辑。

### 17.2 统一异常处理

使用：

```java
@RestControllerAdvice
```

统一处理：

- 参数校验异常。
- 业务异常。
- 权限异常。
- 系统异常。

### 17.3 拦截器和过滤器

Filter：

- Servlet 规范。
- 进入 Spring MVC 前执行。

Interceptor：

- Spring MVC 提供。
- 可获取 Handler 信息。

常见用途：

- 登录校验。
- 日志。
- Trace ID。
- 权限。

## 18. 数据访问

### 18.1 MyBatis

适合复杂 SQL 和国内企业项目。

重点：

- Mapper。
- XML。
- 动态 SQL。
- 参数绑定。
- 结果映射。
- 分页。
- 批量操作。

注意：

- 防止 SQL 注入。
- SQL 要可读。
- 避免 XML 过度复杂。

### 18.2 Spring Data JPA

适合领域模型清晰、CRUD 较多的项目。

重点：

- Entity。
- Repository。
- JPQL。
- Specification。
- 分页排序。
- 事务。

注意：

- 关注 N+1 查询。
- 复杂查询要看 SQL。
- 不要滥用复杂对象关系映射。

### 18.3 MyBatis-Plus

MyBatis-Plus 简化 MyBatis 常见 CRUD。

适合：

- 后台管理系统。
- CRUD 较多项目。

注意：

- 复杂查询仍要写清晰 SQL。
- 不要让自动化 CRUD 掩盖业务建模。

## 19. MySQL 与数据库设计

### 19.1 SQL 基础

必须掌握：

- SELECT。
- INSERT。
- UPDATE。
- DELETE。
- JOIN。
- GROUP BY。
- HAVING。
- ORDER BY。
- LIMIT。
- 子查询。
- 聚合函数。

### 19.2 表设计

重点：

- 主键。
- 唯一索引。
- 普通索引。
- 字段类型。
- 默认值。
- 非空约束。
- 创建时间。
- 更新时间。
- 逻辑删除。

建议：

- 主键稳定。
- 字段命名清晰。
- 金额使用 decimal。
- 状态字段用明确枚举值。
- 大字段谨慎放主表。

### 19.3 索引

必须掌握：

- B+Tree。
- 聚簇索引。
- 二级索引。
- 联合索引。
- 最左前缀。
- 覆盖索引。
- 回表。
- 索引下推。

索引不是越多越好。

索引会：

- 提升查询。
- 占用空间。
- 降低写入性能。

### 19.4 事务

ACID：

- 原子性。
- 一致性。
- 隔离性。
- 持久性。

隔离级别：

- 读未提交。
- 读已提交。
- 可重复读。
- 串行化。

常见问题：

- 脏读。
- 不可重复读。
- 幻读。
- 死锁。

### 19.5 SQL 优化

重点：

- 使用 explain。
- 避免全表扫描。
- 避免 select *。
- 合理索引。
- 避免深分页。
- 控制 join 复杂度。
- 避免函数导致索引失效。

## 20. Redis

### 20.1 Redis 数据结构

必须掌握：

- String。
- Hash。
- List。
- Set。
- ZSet。
- Bitmap。
- HyperLogLog。
- Stream。

### 20.2 常见场景

Redis 常用于：

- 缓存。
- 分布式锁。
- 限流。
- 排行榜。
- 计数器。
- 验证码。
- Session。
- 延迟队列。

### 20.3 缓存问题

常见问题：

- 缓存穿透。
- 缓存击穿。
- 缓存雪崩。
- 缓存不一致。

解决：

- 空值缓存。
- 布隆过滤器。
- 热点 Key 保护。
- 过期时间随机化。
- 互斥锁。
- 延迟双删。

### 20.4 分布式锁

注意：

- 设置过期时间。
- 释放锁时校验归属。
- 业务执行时间不能超过锁超时时间。
- 复杂场景考虑 Redisson。

## 21. 消息队列

### 21.1 作用

消息队列用于：

- 异步处理。
- 削峰填谷。
- 系统解耦。
- 事件驱动。
- 数据同步。

常见产品：

- Kafka。
- RabbitMQ。
- RocketMQ。
- Pulsar。

### 21.2 核心概念

必须掌握：

- Producer。
- Consumer。
- Topic。
- Queue。
- Partition。
- Consumer Group。
- Offset。
- ACK。
- Retry。
- Dead Letter Queue。

### 21.3 可靠性问题

常见问题：

- 消息丢失。
- 消息重复。
- 消息乱序。
- 消息积压。
- 消费失败。

解决：

- 生产确认。
- 消费 ACK。
- 幂等消费。
- 重试机制。
- 死信队列。
- 监控积压。

## 22. Spring Security 与认证授权

### 22.1 认证

认证解决“你是谁”。

方式：

- Session + Cookie。
- JWT。
- OAuth2。
- SSO。
- API Key。

### 22.2 授权

授权解决“你能做什么”。

模型：

- RBAC。
- ABAC。
- ACL。
- 数据权限。

### 22.3 Spring Security

核心：

- SecurityFilterChain。
- Authentication。
- Authorization。
- UserDetailsService。
- PasswordEncoder。
- Method Security。

必须掌握：

- 登录流程。
- 过滤器链。
- 密码加密。
- JWT 校验。
- 权限注解。
- CORS 和 CSRF。

## 23. 测试体系

### 23.1 单元测试

工具：

- JUnit 5。
- Mockito。
- AssertJ。

适合：

- 工具类。
- Service 业务逻辑。
- 状态流转。
- 规则计算。

### 23.2 集成测试

工具：

- Spring Boot Test。
- Testcontainers。
- MockMvc。

适合：

- Controller。
- Repository。
- 数据库访问。
- 缓存。
- 消息队列。

### 23.3 测试原则

建议：

- 核心业务必须测试。
- 测试数据可重复。
- 不依赖本地环境。
- 不用集成测试替代所有单元测试。
- CI 中自动执行关键测试。

## 24. 日志与可观测性

### 24.1 日志

常见框架：

- SLF4J。
- Logback。
- Log4j2。

日志应包含：

- traceId。
- userId。
- 关键参数。
- 异常堆栈。
- 外部调用耗时。
- 业务状态变化。

不要打印：

- 密码。
- 完整 Token。
- 身份证。
- 银行卡。
- 敏感密钥。

### 24.2 指标

指标包括：

- QPS。
- 响应时间。
- P95/P99。
- 错误率。
- JVM 内存。
- GC。
- 线程。
- 数据库连接池。
- Redis 命中率。
- MQ 积压。

### 24.3 链路追踪

工具：

- OpenTelemetry。
- SkyWalking。
- Jaeger。
- Zipkin。

价值：

- 追踪一次请求经过哪些服务。
- 分析慢调用。
- 定位错误链路。

### 24.4 Actuator

Spring Boot Actuator 提供：

- health。
- metrics。
- prometheus。
- loggers。
- env。
- beans。
- threaddump。

生产环境应谨慎暴露敏感端点。

## 25. Docker、Kubernetes 与 DevOps

### 25.1 Docker

Docker 用于容器化应用。

需要掌握：

- Dockerfile。
- Image。
- Container。
- Volume。
- Network。
- Compose。
- 多阶段构建。

Spring Boot Dockerfile 示例：

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 25.2 Kubernetes

需要掌握：

- Pod。
- Deployment。
- Service。
- Ingress。
- ConfigMap。
- Secret。
- HPA。
- readinessProbe。
- livenessProbe。

### 25.3 CI/CD

流程：

```text
提交代码 -> 测试 -> 构建 -> 镜像 -> 部署 -> 健康检查 -> 监控
```

常见工具：

- Jenkins。
- GitLab CI。
- GitHub Actions。
- Argo CD。

## 26. 微服务与 Spring Cloud

### 26.1 微服务是什么

微服务是把一个大系统拆成多个小服务，每个服务围绕独立业务能力开发、部署和扩展。

优点：

- 独立部署。
- 独立扩展。
- 团队边界清晰。
- 故障隔离。

缺点：

- 分布式复杂度高。
- 数据一致性复杂。
- 调用链路变长。
- 运维成本更高。

### 26.2 Spring Cloud 生态

常见组件：

- Spring Cloud Gateway。
- Spring Cloud OpenFeign。
- Spring Cloud LoadBalancer。
- Spring Cloud Config。
- Spring Cloud Circuit Breaker。
- Spring Cloud Consul。
- Spring Cloud Kubernetes。

国内常见：

- Nacos。
- Sentinel。
- Seata。

### 26.3 服务治理

需要掌握：

- 服务注册发现。
- 负载均衡。
- 配置中心。
- 网关。
- 限流。
- 熔断。
- 降级。
- 链路追踪。

## 27. 分布式系统

### 27.1 CAP 和 BASE

CAP：

- 一致性。
- 可用性。
- 分区容错性。

BASE：

- 基本可用。
- 软状态。
- 最终一致性。

后台开发中要理解不同业务的一致性要求。

支付、资金、库存通常要求更严格。

统计、消息、推荐通常可以接受最终一致。

### 27.2 分布式事务

常见方案：

- 2PC。
- TCC。
- Saga。
- 本地消息表。
- 事务消息。
- 最大努力通知。

建议：

- 优先通过业务设计避免强分布式事务。
- 大多数互联网业务使用最终一致性。
- 关键链路要有补偿和对账。

### 27.3 幂等

幂等表示同一操作执行多次结果一致。

场景：

- 支付回调。
- 订单创建。
- 消息消费。
- 表单提交。
- 重试请求。

实现方式：

- 唯一业务号。
- 去重表。
- Redis 去重。
- 数据库唯一索引。
- 状态机判断。

### 27.4 限流、熔断、降级

限流：

- 防止流量压垮系统。

熔断：

- 下游故障时快速失败，避免拖垮上游。

降级：

- 非核心能力暂时关闭，保证核心链路。

## 28. 性能优化与线上排障

### 28.1 接口慢

排查：

- SQL 是否慢。
- 是否缺索引。
- 是否调用外部服务慢。
- 是否锁等待。
- 是否线程池满。
- 是否 GC 频繁。
- 是否网络问题。

### 28.2 CPU 高

排查：

- top 找进程。
- jstack 看线程。
- Arthas 定位热点方法。
- 检查死循环。
- 检查频繁 GC。

### 28.3 内存高

排查：

- 堆内存。
- 直接内存。
- 缓存过大。
- 集合未释放。
- 线程过多。
- 内存泄漏。

### 28.4 数据库问题

常见：

- 慢 SQL。
- 锁等待。
- 死锁。
- 连接池耗尽。
- 大事务。
- 深分页。

解决：

- explain。
- 加索引。
- 改 SQL。
- 分页优化。
- 拆事务。
- 加缓存。

## 29. 安全实践

必须掌握：

- 输入校验。
- SQL 注入防护。
- XSS 防护。
- CSRF 防护。
- 文件上传安全。
- 密码加密。
- Token 安全。
- 权限校验。
- 数据权限。
- 日志脱敏。
- 依赖漏洞扫描。

关键原则：

- 不信任客户端。
- 前端权限不是安全边界。
- 密码不能明文存储。
- 敏感配置不能提交仓库。
- 所有接口都要做权限校验。

## 30. 架构设计和 DDD

### 30.1 分层架构

常见分层：

```text
Controller -> Application Service -> Domain Service -> Repository
```

简单项目可以：

```text
Controller -> Service -> Repository
```

### 30.2 DDD

DDD 是领域驱动设计，适合复杂业务系统。

核心概念：

- 领域。
- 子域。
- 限界上下文。
- 实体。
- 值对象。
- 聚合。
- 聚合根。
- 领域服务。
- 领域事件。
- 仓储。

价值：

- 让代码模型贴近业务。
- 降低复杂业务维护成本。
- 明确业务边界。

### 30.3 架构能力

高级后台工程师需要：

- 能做技术选型。
- 能设计模块边界。
- 能识别系统瓶颈。
- 能制定演进路线。
- 能控制技术债务。
- 能写技术方案和复盘。

## 31. 推荐实践项目

### 31.1 用户权限系统

覆盖：

- 用户。
- 角色。
- 权限。
- 菜单。
- 登录。
- JWT。
- RBAC。
- 数据权限。

### 31.2 电商订单系统

覆盖：

- 商品。
- 库存。
- 购物车。
- 订单。
- 支付回调。
- 超时关单。
- 消息队列。
- 幂等。
- 事务。

### 31.3 秒杀系统

覆盖：

- Redis。
- 限流。
- MQ 削峰。
- 库存扣减。
- 防重复下单。
- 异步下单。
- 热点 Key。

### 31.4 多租户 SaaS 系统

覆盖：

- 租户模型。
- 数据隔离。
- 权限隔离。
- 配置隔离。
- 审计日志。

### 31.5 微服务项目

覆盖：

- 注册发现。
- 网关。
- 配置中心。
- Feign 调用。
- 限流熔断。
- 链路追踪。
- 分布式事务。

## 32. 分阶段学习路线

### 32.1 第一阶段：Java 基础

目标：

- 能写清晰 Java 代码。

重点：

- 语法。
- 面向对象。
- 集合。
- 异常。
- 泛型。
- Stream。
- Maven。

### 32.2 第二阶段：Web 和数据库

目标：

- 能写基本后端接口。

重点：

- HTTP。
- REST API。
- MySQL。
- SQL。
- 索引。
- Spring MVC。
- MyBatis/JPA。

### 32.3 第三阶段：Spring Boot 工程开发

目标：

- 能独立完成业务模块。

重点：

- Spring Boot。
- 参数校验。
- 异常处理。
- 事务。
- Redis。
- 日志。
- 单元测试。
- 部署。

### 32.4 第四阶段：中高级后台能力

目标：

- 能处理高并发、稳定性和线上问题。

重点：

- JVM。
- 并发。
- 线程池。
- MQ。
- 缓存一致性。
- 安全。
- 监控。
- 性能优化。

### 32.5 第五阶段：架构和分布式

目标：

- 能设计复杂系统。

重点：

- 微服务。
- Spring Cloud。
- 分布式事务。
- 限流熔断。
- DDD。
- Docker/Kubernetes。
- 可观测性。

## 33. 能力自检清单

你应该能够回答：

- HashMap 的底层结构是什么？
- ArrayList 和 LinkedList 有什么区别？
- synchronized 和 ReentrantLock 有什么区别？
- volatile 能保证原子性吗？
- 线程池核心参数有哪些？
- JVM 内存区域有哪些？
- Full GC 如何排查？
- Spring Bean 生命周期是什么？
- Spring Boot 自动配置如何生效？
- `@Transactional` 为什么会失效？
- MySQL 索引为什么能加速查询？
- Redis 缓存穿透、击穿、雪崩如何解决？
- MQ 如何保证不丢消息？
- 消息重复消费如何处理？
- JWT 和 Session 有什么区别？
- 如何设计统一异常和错误码？
- 如何设计一个订单系统？
- 如何排查接口慢？
- 如何部署 Spring Boot 到 Docker？
- 微服务什么时候该拆，什么时候不该拆？

## 34. 总结

Java 后台开发是一套完整工程能力，不只是 Java 语法，也不只是 Spring Boot。它需要语言基础、数据库能力、中间件能力、Web 协议、安全意识、测试能力、部署能力、监控排障能力和架构设计能力共同支撑。

学习时不要只背知识点，要用项目把知识串起来。一个用户权限系统可以训练认证授权和 RBAC，一个订单系统可以训练事务、状态机和幂等，一个秒杀系统可以训练 Redis、MQ、限流和高并发，一个微服务项目可以训练服务治理、链路追踪和分布式一致性。

真正的后台工程师能力，是能把业务需求转成稳定、清晰、可维护、可扩展、可监控的后端系统，并能在问题发生时快速定位、修复和复盘。
