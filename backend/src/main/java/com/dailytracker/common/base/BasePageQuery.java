package com.dailytracker.common.base;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询参数基类 - 所有分页查询请求的公共参数
 *
 * 【类的用途】
 * 封装分页查询的通用参数（页码和每页条数），所有需要分页的查询请求 DTO
 * 都可以继承此类，无需在每个 DTO 中重复定义分页参数。
 *
 * 【设计思想】
 * 1. 使用 JSR-303 Bean Validation 注解进行参数校验，无需手动编写校验代码
 * 2. 提供默认值，前端不传分页参数时也能正常工作
 * 3. 限制每页最大条数，防止一次查询过多数据导致性能问题
 *
 * 【在架构中的位置】
 * 属于通用基础层，被所有分页查询的 DTO（如 PlanPageQuery、AccountingPageQuery）继承。
 * 位于请求层（Request Layer），用于接收前端的分页查询参数。
 *
 * 【使用示例】
 * // 定义分页查询 DTO
 * public class PlanPageQuery extends BasePageQuery {
 *     private String keyword;    // 额外的搜索条件
 *     private String status;     // 状态过滤
 * }
 *
 * // 在 Controller 中使用（配合 @Valid 触发校验）
 * @GetMapping("/plans")
 * public Result<PageResult<PlanVO>> listPlans(@Valid PlanPageQuery query) {
 *     // query.getPageNum() 和 query.getPageSize() 已由基类提供
 *     return planService.pageList(query);
 * }
 *
 * 【相关技术知识点 - Bean Validation】
 * Bean Validation 是 Java 标准的参数校验规范（JSR-303/JSR-380）：
 * - @Min：最小值校验
 * - @Max：最大值校验
 * - @NotBlank：非空字符串
 * - @NotNull：非 null
 * - @Size：字符串/集合长度范围
 * - @Email：邮箱格式
 * - @Pattern：正则表达式匹配
 *
 * @Data Lombok 注解，自动生成 getter、setter 方法
 */
@Data
public class BasePageQuery {

    /**
     * 当前页码（从1开始）
     *
     * @Min(value = 1, message = "页码最小为1")
     * Bean Validation 注解，校验此值必须 >= 1
     * 如果前端传入 0 或负数，校验会失败并返回错误消息 "页码最小为1"
     *
     * 默认值为 1，即第一页。前端不传此参数时默认查询第一页。
     */
    @Min(value = 1, message = "页码最小为1")
    private long pageNum = 1;

    /**
     * 每页显示的记录条数
     *
     * @Min(value = 1, message = "每页条数最小为1") 最小值限制为 1
     * @Max(value = 100, message = "每页条数最大为100") 最大值限制为 100
     *
     * 【为什么要限制最大值？】
     * 防止前端传入过大的 pageSize（如 999999）导致一次查询返回海量数据，
     * 造成内存溢出（OOM）或数据库性能问题。这是一种防御性编程策略。
     *
     * 默认值为 20，这是一个合理的默认分页大小。
     */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private long pageSize = 20;
}
