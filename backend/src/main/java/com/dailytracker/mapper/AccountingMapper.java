package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.Accounting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记账明细数据访问层（Mapper接口）- 提供记账明细表（t_accounting）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link Accounting}</li>
 *   <li>数据库表：t_accounting</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>与 t_accounting_category 表为多对一关系（通过 categoryId 关联）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <Accounting>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(Accounting entity)} - 新增一笔收支记录</li>
 *   <li>{@code selectById(Long id)} - 根据ID查询某笔记录</li>
 *   <li>{@code selectList(Wrapper)} - 按条件查询收支列表（如按用户、按日期范围、按分类等）</li>
 *   <li>{@code selectPage(Page, Wrapper)} - 分页查询收支列表</li>
 *   <li>{@code updateById(Accounting entity)} - 更新收支记录</li>
 *   <li>{@code deleteById(Long id)} - 删除收支记录（逻辑删除）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>记录一笔支出：设置 type="EXPENSE"、amount、categoryId 等，调用 {@code insert}</li>
 *   <li>查询某月所有收支：使用 QueryWrapper 的 between 条件查询 accountingDate 范围</li>
 *   <li>统计某月总支出：使用 QueryWrapper 的 select("IFNULL(SUM(amount),0)") 配合 groupBy</li>
 *   <li>按分类统计：需要自定义 SQL 进行 GROUP BY category_id 查询</li>
 * </ul>
 *
 * <p><b>【如何自定义统计SQL】</b></p>
 * <p>记账模块经常需要复杂的统计查询（如月度收支汇总、分类统计、趋势分析等），
 * 这些查询超出了 BaseMapper 单表 CRUD 的能力范围。
 * 可以在本接口中添加自定义方法，使用 @Select 注解或在 XML 中编写聚合查询 SQL。</p>
 *
 * @see Accounting 记账明细实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface AccountingMapper extends BaseMapper<Accounting> {
}
