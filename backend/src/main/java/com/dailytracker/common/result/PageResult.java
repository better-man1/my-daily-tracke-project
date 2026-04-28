package com.dailytracker.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页数据封装类，作为 Result 的 data 字段使用
 */
@Data
public class PageResult<T> {

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页 */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    public static <T> Result<PageResult<T>> of(List<T> records, long total, long pageNum, long pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(total);
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setPages(pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize);
        return Result.success(pageResult);
    }
}
