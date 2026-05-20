package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层（Mapper接口）- 提供用户表（t_user）的数据库操作方法
 *
 * <p><b>【MyBatis-Plus BaseMapper 简介】</b></p>
 * <p>本接口继承自 {@link BaseMapper}{@code <User>}，MyBatis-Plus 会自动为 User 实体生成
 * 完整的单表 CRUD（增删改查）SQL 语句，无需开发者手写任何 SQL。
 * 这是 MyBatis-Plus 的核心特性之一："单表操作零 SQL"。</p>
 *
 * <p><b>【为什么接口不需要写实现类？】</b></p>
 * <p>MyBatis-Plus 在应用启动时会通过动态代理（Dynamic Proxy）机制，自动为继承 BaseMapper 的接口
 * 生成实现类。开发者只需定义接口，无需编写实现类或 XML 映射文件。</p>
 *
 * <p><b>【BaseMapper 自动提供的方法列表】</b></p>
 * <ul>
 *   <li><b>插入</b>：{@code insert(User entity)} - 插入一条记录</li>
 *   <li><b>删除</b>：
 *     <ul>
 *       <li>{@code deleteById(Serializable id)} - 根据ID删除</li>
 *       <li>{@code deleteByMap(Map<String, Object> columnMap)} - 根据列条件删除</li>
 *       <li>{@code delete(Wrapper<User> wrapper)} - 根据条件构造器删除</li>
 *       <li>{@code deleteBatchIds(Collection<?> idList)} - 根据ID列表批量删除</li>
 *     </ul>
 *   </li>
 *   <li><b>修改</b>：
 *     <ul>
 *       <li>{@code updateById(User entity)} - 根据ID更新（只更新非null字段）</li>
 *       <li>{@code update(User entity, Wrapper<User> wrapper)} - 根据条件更新</li>
 *     </ul>
 *   </li>
 *   <li><b>查询</b>：
 *     <ul>
 *       <li>{@code selectById(Serializable id)} - 根据ID查询</li>
 *       <li>{@code selectBatchIds(Collection<?> idList)} - 根据ID列表批量查询</li>
 *       <li>{@code selectByMap(Map<String, Object> columnMap)} - 根据列条件查询</li>
 *       <li>{@code selectOne(Wrapper<User> wrapper)} - 根据条件查询单条记录</li>
 *       <li>{@code selectList(Wrapper<User> wrapper)} - 根据条件查询列表</li>
 *       <li>{@code selectCount(Wrapper<User> wrapper)} - 根据条件查询总数</li>
 *       <li>{@code selectPage(Page<User> page, Wrapper<User> wrapper)} - 分页查询</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>【如何自定义SQL？】</b></p>
 * <p>当 BaseMapper 提供的方法无法满足需求时，可以通过以下方式自定义 SQL：
 * <ul>
 *   <li><b>注解方式</b>：在方法上使用 @Select/@Insert/@Update/@Delete 注解直接写 SQL</li>
 *   <li><b>XML方式</b>：在 resources/mapper/ 目录下创建对应的 XML 文件编写 SQL</li>
 *   <li><b>Wrapper方式</b>：使用 QueryWrapper 或 LambdaQueryWrapper 构建复杂查询条件</li>
 * </ul>
 * </p>
 *
 * <p><b>【@Mapper 注解说明】</b></p>
 * <p>{@code @Mapper} 是 MyBatis（非 MyBatis-Plus）提供的注解，作用：
 * <ul>
 *   <li>标记此接口为 MyBatis 的 Mapper 接口</li>
 *   <li>让 MyBatis 在启动时扫描并为此接口创建动态代理实现类</li>
 *   <li>如果不使用 @Mapper 注解，也可以在启动类上使用 @MapperScan("com.dailytracker.mapper") 批量扫描</li>
 * </ul>
 *
 * <p><b>【逻辑删除的影响】</b></p>
 * <p>由于 User 实体继承了 BaseEntity（包含 @TableLogic 注解的 isDeleted 字段），
 * BaseMapper 的所有 SELECT 方法会自动追加 WHERE is_deleted = 0 条件，
 * DELETE 方法会自动转为 UPDATE SET is_deleted = 1。这是透明的，开发者无需额外处理。</p>
 *
 * @see User 用户实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 * @see com.baomidou.mybatisplus.core.conditions.query.QueryWrapper 条件构造器
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
