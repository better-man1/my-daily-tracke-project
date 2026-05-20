package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.AccountingCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记账分类数据访问层（Mapper接口）- 提供记账分类表（t_accounting_category）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link AccountingCategory}</li>
 *   <li>数据库表：t_accounting_category</li>
 *   <li>包含系统预设分类（userId=null）和用户自定义分类（userId=具体值）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <AccountingCategory>} 后，自动拥有完整的单表操作方法。
 * 注意：AccountingCategory 未继承 BaseEntity，没有逻辑删除功能，
 * 因此 deleteById 会执行真正的物理删除（DELETE FROM）。</p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>查询所有可用分类：{@code selectList(new QueryWrapper<>().isNull("userId").or().eq("userId", currentUserId))}</li>
 *   <li>查询系统预设分类：{@code selectList(new QueryWrapper<>().isNull("userId"))}</li>
 *   <li>查询用户自定义分类：{@code selectList(new QueryWrapper<>().eq("userId", userId))}</li>
 *   <li>新增用户自定义分类：设置 name、type、icon 等，调用 {@code insert}</li>
 * </ul>
 *
 * <p><b>【特别注意 - 无逻辑删除】</b></p>
 * <p>由于 AccountingCategory 未继承 BaseEntity，没有 @TableLogic 注解字段，
 * 所有删除操作都是物理删除。删除分类前应检查是否有关联的记账记录。</p>
 *
 * @see AccountingCategory 记账分类实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface AccountingCategoryMapper extends BaseMapper<AccountingCategory> {
}
