package com.dailytracker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.dto.request.ExcerptCreateRequest;
import com.dailytracker.dto.response.ExcerptResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日摘录服务接口
 */
public interface ExcerptService {

    /** 创建摘录 */
    ExcerptResponse create(ExcerptCreateRequest request);

    /** 分页查询摘录列表 */
    IPage<ExcerptResponse> page(int pageNum, int pageSize, LocalDate startDate, LocalDate endDate,
                                String sourceType, Long tagId, Integer isFavorite);

    /** 摘录详情 */
    ExcerptResponse getById(Long id);

    /** 更新摘录 */
    ExcerptResponse update(Long id, ExcerptCreateRequest request);

    /** 删除摘录 */
    void delete(Long id);

    /** 切换收藏状态 */
    void toggleFavorite(Long id);

    /** 随机获取一条摘录 */
    ExcerptResponse getRandom();

    /** 获取用户所有标签 */
    List<Map<String, Object>> getAllTags();

    /** 全文搜索 */
    IPage<ExcerptResponse> search(String keyword, int pageNum, int pageSize);

    /** 导出摘录为 Markdown 格式 */
    String exportMarkdown(Long userId);
}
