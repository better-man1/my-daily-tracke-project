package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.ExcerptCreateRequest;
import com.dailytracker.dto.response.ExcerptResponse;
import com.dailytracker.entity.Excerpt;
import com.dailytracker.entity.ExcerptTagRel;
import com.dailytracker.entity.Tag;
import com.dailytracker.mapper.ExcerptMapper;
import com.dailytracker.mapper.ExcerptTagRelMapper;
import com.dailytracker.mapper.TagMapper;
import com.dailytracker.service.ExcerptService;
import com.dailytracker.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日摘录服务实现类（Excerpt Service Implementation）
 *
 * 【类设计说明】
 * 本类是 ExcerptService 接口的具体实现，负责摘录/读书笔记模块的全部业务逻辑。
 * 包括摘录 CRUD、收藏管理、标签管理、多条件筛选分页、全文搜索、Markdown 导出等。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J 日志记录器
 * @Service    - Spring 注解，标记为业务层 Bean
 * @RequiredArgsConstructor - Lombok 注解，通过构造器注入依赖
 *
 * 【核心设计】
 * - 多对多关联管理：通过 ExcerptTagRel 中间表管理摘录和标签的关联关系
 * - 标签使用计数：每次关联标签时自动递增 usageCount，支持按使用频率排序
 * - 全文搜索：使用 SQL LIKE 模糊匹配（适用于中小数据量场景）
 * - 数据导出：格式化为 Markdown 格式，便于备份和跨平台使用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcerptServiceImpl implements ExcerptService {

    /** 摘录数据访问层 */
    private final ExcerptMapper excerptMapper;
    /** 标签数据访问层 */
    private final TagMapper tagMapper;
    /** 摘录-标签关联数据访问层 */
    private final ExcerptTagRelMapper excerptTagRelMapper;
    /** Jackson JSON 序列化工具 */
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ExcerptResponse create(ExcerptCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Excerpt excerpt = new Excerpt();
        BeanUtils.copyProperties(request, excerpt, "images", "tagIds");
        excerpt.setUserId(userId);
        // 图片列表序列化为 JSON
        if (request.getImages() != null) {
            excerpt.setImages(toJson(request.getImages()));
        }
        excerptMapper.insert(excerpt);

        // 处理标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveTagRelations(excerpt.getId(), request.getTagIds(), userId);
        }

        log.info("创建摘录成功: userId={}, id={}", userId, excerpt.getId());
        return toResponse(excerpt);
    }

    @Override
    public IPage<ExcerptResponse> page(int pageNum, int pageSize, LocalDate startDate, LocalDate endDate,
                                       String sourceType, Long tagId, Integer isFavorite) {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<Excerpt> wrapper = new LambdaQueryWrapper<Excerpt>()
                .eq(Excerpt::getUserId, userId)
                .ge(startDate != null, Excerpt::getExcerptDate, startDate)
                .le(endDate != null, Excerpt::getExcerptDate, endDate)
                .eq(sourceType != null, Excerpt::getSourceType, sourceType)
                .eq(isFavorite != null, Excerpt::getIsFavorite, isFavorite)
                .orderByDesc(Excerpt::getExcerptDate)
                .orderByDesc(Excerpt::getId);

        // 若按标签过滤，先查标签关联的摘录ID列表
        if (tagId != null) {
            List<Long> excerptIds = excerptTagRelMapper.selectList(
                    new LambdaQueryWrapper<ExcerptTagRel>().eq(ExcerptTagRel::getTagId, tagId)
            ).stream().map(ExcerptTagRel::getExcerptId).collect(Collectors.toList());
            if (excerptIds.isEmpty()) {
                return new Page<ExcerptResponse>(pageNum, pageSize).setRecords(Collections.emptyList());
            }
            wrapper.in(Excerpt::getId, excerptIds);
        }

        IPage<Excerpt> excerptPage = excerptMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return excerptPage.convert(this::toResponse);
    }

    @Override
    public ExcerptResponse getById(Long id) {
        return toResponse(getAndValidate(id));
    }

    @Override
    @Transactional
    public ExcerptResponse update(Long id, ExcerptCreateRequest request) {
        Excerpt excerpt = getAndValidate(id);
        BeanUtils.copyProperties(request, excerpt, "id", "userId", "images", "tagIds");
        if (request.getImages() != null) {
            excerpt.setImages(toJson(request.getImages()));
        }
        excerptMapper.updateById(excerpt);

        // 更新标签关联
        if (request.getTagIds() != null) {
            excerptTagRelMapper.delete(
                    new LambdaQueryWrapper<ExcerptTagRel>().eq(ExcerptTagRel::getExcerptId, id));
            if (!request.getTagIds().isEmpty()) {
                saveTagRelations(id, request.getTagIds(), excerpt.getUserId());
            }
        }
        return toResponse(excerpt);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getAndValidate(id);
        excerptTagRelMapper.delete(
                new LambdaQueryWrapper<ExcerptTagRel>().eq(ExcerptTagRel::getExcerptId, id));
        excerptMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleFavorite(Long id) {
        Excerpt excerpt = getAndValidate(id);
        excerpt.setIsFavorite(excerpt.getIsFavorite() == 1 ? 0 : 1);
        excerptMapper.updateById(excerpt);
    }

    @Override
    public ExcerptResponse getRandom() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Excerpt> all = excerptMapper.selectList(
                new LambdaQueryWrapper<Excerpt>().eq(Excerpt::getUserId, userId));
        if (all.isEmpty()) return null;
        return toResponse(all.get(new Random().nextInt(all.size())));
    }

    @Override
    public List<Map<String, Object>> getAllTags() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getUserId, userId)
                        .orderByDesc(Tag::getUsageCount));
        return tags.stream().map(this::tagToMap).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> createTag(String name, String color) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 检查是否已存在同名标签
        Tag existing = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId)
                .eq(Tag::getName, name));
        if (existing != null) {
            return tagToMap(existing);
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color != null ? color : "#6366f1");
        tag.setUsageCount(0);
        tag.setCreatedAt(java.time.LocalDateTime.now());
        tagMapper.insert(tag);

        return tagToMap(tag);
    }

    /**
     * Tag 实体转 Map（私有工具方法）
     *
     * 将标签实体转换为前端友好的 Map 结构。
     *
     * @param t 标签实体
     * @return 包含 id、name、color、usageCount 的 Map
     */
    private Map<String, Object> tagToMap(Tag t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("name", t.getName());
        map.put("color", t.getColor());
        map.put("usageCount", t.getUsageCount());
        return map;
    }

    @Override
    public IPage<ExcerptResponse> search(String keyword, int pageNum, int pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Excerpt> wrapper = new LambdaQueryWrapper<Excerpt>()
                .eq(Excerpt::getUserId, userId)
                .and(w -> w.like(Excerpt::getContent, keyword)
                        .or().like(Excerpt::getThought, keyword)
                        .or().like(Excerpt::getSourceTitle, keyword))
                .orderByDesc(Excerpt::getExcerptDate);
        IPage<Excerpt> excerptPage = excerptMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return excerptPage.convert(this::toResponse);
    }

    @Override
    public String exportMarkdown(Long userId) {
        List<Excerpt> list = excerptMapper.selectList(
                new LambdaQueryWrapper<Excerpt>()
                        .eq(Excerpt::getUserId, userId)
                        .orderByDesc(Excerpt::getExcerptDate));
        
        StringBuilder sb = new StringBuilder();
        sb.append("# 每日摘录备份\n\n");
        sb.append("> 导出日期: ").append(LocalDate.now()).append("\n\n");
        
        for (Excerpt excerpt : list) {
            sb.append("## ").append(excerpt.getSourceTitle() != null ? excerpt.getSourceTitle() : "未命名来源").append("\n");
            sb.append("- **日期**: ").append(excerpt.getExcerptDate()).append("\n");
            sb.append("- **类型**: ").append(excerpt.getSourceType()).append("\n");
            if (excerpt.getIsFavorite() == 1) {
                sb.append("- **收藏**: ❤️\n");
            }
            sb.append("\n### 原文\n");
            sb.append(excerpt.getContent()).append("\n\n");
            if (excerpt.getThought() != null && !excerpt.getThought().isEmpty()) {
                sb.append("### 心得\n");
                sb.append(excerpt.getThought()).append("\n\n");
            }
            sb.append("---\n\n");
        }
        
        return sb.toString();
    }

    // =================== 私有辅助方法 ===================

    /**
     * 查询并校验摘录归属（私有方法）
     *
     * @param id 摘录ID
     * @return Excerpt 摘录实体
     * @throws BusinessException 摘录不存在或不属于当前用户时抛出异常
     */
    private Excerpt getAndValidate(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Excerpt excerpt = excerptMapper.selectOne(
                new LambdaQueryWrapper<Excerpt>()
                        .eq(Excerpt::getId, id)
                        .eq(Excerpt::getUserId, userId));
        if (excerpt == null) {
            throw new BusinessException(ResultCode.EXCERPT_NOT_FOUND);
        }
        return excerpt;
    }

    /**
     * 保存摘录与标签的关联关系（私有方法）
     *
     * 批量创建关联记录，同时递增每个标签的使用计数。
     *
     * @param excerptId 摘录ID
     * @param tagIds    标签ID列表
     * @param userId    用户ID（用于校验标签归属）
     */
    private void saveTagRelations(Long excerptId, List<Long> tagIds, Long userId) {
        tagIds.forEach(tagId -> {
            ExcerptTagRel rel = new ExcerptTagRel();
            rel.setExcerptId(excerptId);
            rel.setTagId(tagId);
            excerptTagRelMapper.insert(rel);
            // 增加标签使用次数
            Tag tag = tagMapper.selectById(tagId);
            if (tag != null && tag.getUserId().equals(userId)) {
                tag.setUsageCount(tag.getUsageCount() + 1);
                tagMapper.updateById(tag);
            }
        });
    }

    /**
     * 实体转响应 DTO（私有方法）
     *
     * 将摘录实体转换为响应 DTO，同时查询关联的标签列表。
     * 标签查询通过中间表（excerpt_tag_rel）关联。
     *
     * @param excerpt 摘录实体
     * @return ExcerptResponse 响应 DTO（含标签列表）
     */
    private ExcerptResponse toResponse(Excerpt excerpt) {
        ExcerptResponse response = new ExcerptResponse();
        BeanUtils.copyProperties(excerpt, response);
        // 查询标签
        List<ExcerptTagRel> rels = excerptTagRelMapper.selectList(
                new LambdaQueryWrapper<ExcerptTagRel>().eq(ExcerptTagRel::getExcerptId, excerpt.getId()));
        if (!rels.isEmpty()) {
            List<Long> tagIds = rels.stream().map(ExcerptTagRel::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            response.setTags(tags.stream().map(t -> {
                ExcerptResponse.TagInfo info = new ExcerptResponse.TagInfo();
                info.setId(t.getId());
                info.setName(t.getName());
                info.setColor(t.getColor());
                return info;
            }).collect(Collectors.toList()));
        } else {
            response.setTags(Collections.emptyList());
        }
        return response;
    }

    /**
     * 对象转 JSON 字符串（私有工具方法）
     *
     * @param obj 待序列化的对象
     * @return JSON 字符串，失败时返回 null
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }
}
