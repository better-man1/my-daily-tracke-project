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
 * 每日摘录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcerptServiceImpl implements ExcerptService {

    private final ExcerptMapper excerptMapper;
    private final TagMapper tagMapper;
    private final ExcerptTagRelMapper excerptTagRelMapper;
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
        return tags.stream().map(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("color", t.getColor());
            map.put("usageCount", t.getUsageCount());
            return map;
        }).collect(Collectors.toList());
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

    // =================== 私有方法 ===================

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

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }
}
