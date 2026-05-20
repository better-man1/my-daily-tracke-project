package com.dailytracker.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页数据封装类 - 用于包装分页查询的结果数据
 *
 * 【类的用途】
 * 封装分页查询的所有信息，包括：
 * - 当前页的数据列表（records）
 * - 分页元数据（总记录数、当前页码、每页大小、总页数）
 *
 * 通常作为 Result<T> 中 T 的具体类型使用：
 * Result<PageResult<UserVO>> - 表示"分页查询用户列表"的响应
 *
 * 【设计思想】
 * 1. 将分页信息与数据一起返回，前端可以据此渲染分页控件
 * 2. 提供静态工厂方法 of() 简化创建过程，自动计算总页数
 * 3. 与 MyBatis-Plus 的 Page 对象解耦，Controller 层统一使用此 VO
 *
 * 【在架构中的位置】
 * 属于通用层，在 Service 层和 Controller 层之间传递分页数据。
 * 数据流转：MyBatis-Plus Page -> Service 转换 -> PageResult -> Result -> 前端
 *
 * 【前端使用示例】
 * 前端拿到响应后，可以使用以下信息渲染分页控件：
 * {
 *   "code": 200,
 *   "data": {
 *     "records": [...],     // 当前页的数据列表
 *     "total": 150,         // 总共 150 条记录
 *     "pageNum": 1,         // 当前是第 1 页
 *     "pageSize": 20,       // 每页 20 条
 *     "pages": 8            // 总共 8 页（150 / 20 = 7.5，向上取整为 8）
 *   }
 * }
 *
 * @Data Lombok 注解，自动生成 getter、setter、toString、equals、hashCode 方法
 */
@Data
public class PageResult<T> {

    /**
     * 当前页的数据列表
     * 泛型 T 表示列表中元素的类型，如 UserVO、PlanVO、GoalVO 等
     */
    private List<T> records;

    /**
     * 总记录数 - 满足查询条件的记录总数（不考虑分页）
     * 用于计算总页数和显示"共 X 条"
     */
    private long total;

    /**
     * 当前页码 - 从 1 开始
     * 与前端传来的 pageNum 对应
     */
    private long pageNum;

    /**
     * 每页大小 - 每页显示的记录条数
     * 与前端传来的 pageSize 对应
     */
    private long pageSize;

    /**
     * 总页数 - 根据总记录数和每页大小计算得出
     * 计算公式：pages = ceil(total / pageSize)
     * 用于前端渲染分页控件的页码按钮
     */
    private long pages;

    /**
     * 静态工厂方法 - 创建分页结果并包装为统一响应
     *
     * 【方法作用】
     * 将分页查询的原始数据封装为 PageResult 对象，并自动包装在 Result 中返回。
     * 自动计算总页数，减少手动计算出错的可能。
     *
     * 【使用示例】
     * // 在 Service 层
     * return PageResult.of(userList, total, pageNum, pageSize);
     *
     * // 等价于：
     * PageResult<UserVO> pr = new PageResult<>();
     * pr.setRecords(userList);
     * pr.setTotal(total);
     * pr.setPageNum(pageNum);
     * pr.setPageSize(pageSize);
     * pr.setPages((total + pageSize - 1) / pageSize);
     * return Result.success(pr);
     *
     * @param records  当前页的数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param <T>      列表元素类型
     * @return Result<PageResult<T>> 包装好的分页响应
     */
    public static <T> Result<PageResult<T>> of(List<T> records, long total, long pageNum, long pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(total);
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);

        // 计算总页数：(total + pageSize - 1) / pageSize
        // 这是经典的"向上取整"计算公式，等价于 Math.ceil((double)total / pageSize)
        // 例如：total=150, pageSize=20 -> (150+20-1)/20 = 169/20 = 8 页
        // 特殊处理：pageSize 为 0 时总页数也为 0，避免除零错误
        pageResult.setPages(pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize);

        // 将 PageResult 包装在统一的 Result 响应体中返回
        return Result.success(pageResult);
    }
}
