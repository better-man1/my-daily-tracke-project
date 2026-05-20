package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好设置数据访问层（Mapper接口）- 提供用户设置表（t_user_settings）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link UserSettings}</li>
 *   <li>数据库表：t_user_settings</li>
 *   <li>与 t_user 表为一对一关系（通过 userId 关联）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <UserSettings>} 后，自动拥有以下方法（无需手写SQL）：
 * <ul>
 *   <li>{@code insert(UserSettings entity)} - 插入用户设置记录</li>
 *   <li>{@code selectById(Long id)} - 根据主键ID查询设置</li>
 *   <li>{@code selectOne(Wrapper)} - 根据条件查询单条设置（如按 userId 查询）</li>
 *   <li>{@code updateById(UserSettings entity)} - 根据ID更新设置（只更新非null字段）</li>
 *   <li>{@code deleteById(Long id)} - 根据ID删除设置（逻辑删除，因为继承了 BaseEntity）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>用户首次注册时创建默认设置：{@code mapper.insert(defaultSettings)}</li>
 *   <li>加载用户设置：{@code mapper.selectOne(new QueryWrapper<UserSettings>().eq("userId", userId))}</li>
 *   <li>更新用户设置：{@code mapper.updateById(settings)}</li>
 * </ul>
 *
 * <p><b>【@Mapper 注解说明】</b></p>
 * <p>{@code @Mapper} 标记此接口为 MyBatis 的 Mapper 接口，
 * 启动时 MyBatis 会自动为该接口创建代理实现类。
 * 也可以通过 @MapperScan 注解在启动类上批量扫描。</p>
 *
 * @see UserSettings 用户偏好设置实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {
}
