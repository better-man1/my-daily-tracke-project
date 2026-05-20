package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.Budget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算数据访问层（Mapper接口）- 提供预算表（t_budget）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link Budget}</li>
 *   <li>数据库表：t_budget</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>与 t_accounting_category 表为多对一关系（通过 categoryId 关联，categoryId 为 null 时为总预算）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <Budget>} 后，自动拥有完整的单表操作方法。
 * 注意：Budget 未继承 BaseEntity，没有逻辑删除功能。</p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>设置月度预算：设置 userId、budgetYear、budgetMonth、amount，调用 {@code insert}</li>
 *   <li>查询某月所有预算：{@code selectList(new QueryWrapper<Budget>().eq("userId", userId).eq("budgetYear", year).eq("budgetMonth", month))}</li>
 *   <li>查询某月总预算：{@code selectOne(new QueryWrapper<Budget>().eq("userId", userId).eq("budgetYear", year).eq("budgetMonth", month).isNull("categoryId"))}</li>
 *   <li>更新预算金额：修改 amount 后调用 {@code updateById}</li>
 * </ul>
 *
 * <p><b>【如何计算预算执行情况】</b></p>
 * <p>计算预算使用进度需要联合 t_accounting 表统计当月实际支出：
 * <ul>
 *   <li>总预算使用 = SUM(accounting.amount) WHERE userId=? AND type='EXPENSE' AND accountingDate BETWEEN 月初 AND 月末</li>
 *   <li>分类预算使用 = SUM(accounting.amount) WHERE userId=? AND categoryId=? AND type='EXPENSE' AND accountingDate BETWEEN 月初 AND 月末</li>
 * </ul>
 * 这种跨表统计查询需要在 Service 层组合使用 AccountingMapper 和 BudgetMapper，或编写自定义 JOIN SQL。</p>
 *
 * @see Budget 预算实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {
}
