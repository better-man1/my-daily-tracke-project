package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.TagCreateRequest;
import com.dailytracker.dto.response.TagResponse;
import com.dailytracker.entity.PlanTag;
import com.dailytracker.mapper.PlanTagMapper;
import com.dailytracker.mapper.PlanTagRelationMapper;
import com.dailytracker.service.PlanTagService;
import com.dailytracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 计划标签服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanTagServiceImpl implements PlanTagService {

    private final PlanTagMapper tagMapper;
    private final PlanTagRelationMapper relationMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public TagResponse create(TagCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查标签名称是否已存在
        boolean exists = tagMapper.selectCount(
                new LambdaQueryWrapper<PlanTag>()
                        .eq(PlanTag::getUserId, userId)
                        .eq(PlanTag::getName, request.getName())
        ) > 0;

        if (exists) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "标签名称已存在");
        }

        PlanTag tag = new PlanTag();
        tag.setUserId(userId);
        tag.setName(request.getName());
        tag.setColor(request.getColor() != null ? request.getColor() : "#6366f1");
        tagMapper.insert(tag);

        log.info("创建标签: id={}, name={}", tag.getId(), tag.getName());
        return toResponse(tag);
    }

    @Override
    public List<TagResponse> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<PlanTag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<PlanTag>()
                        .eq(PlanTag::getUserId, userId)
                        .orderByAsc(PlanTag::getName)
        );
        return tags.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagResponse update(Long id, TagCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        PlanTag tag = tagMapper.selectOne(
                new LambdaQueryWrapper<PlanTag>()
                        .eq(PlanTag::getId, id)
                        .eq(PlanTag::getUserId, userId)
        );

        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 检查新名称是否与其他标签冲突
        if (!tag.getName().equals(request.getName())) {
            boolean exists = tagMapper.selectCount(
                    new LambdaQueryWrapper<PlanTag>()
                            .eq(PlanTag::getUserId, userId)
                            .eq(PlanTag::getName, request.getName())
                            .ne(PlanTag::getId, id)
            ) > 0;

            if (exists) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "标签名称已存在");
            }
        }

        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tagMapper.updateById(tag);

        log.info("更新标签: id={}", id);
        return toResponse(tag);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        PlanTag tag = tagMapper.selectOne(
                new LambdaQueryWrapper<PlanTag>()
                        .eq(PlanTag::getId, id)
                        .eq(PlanTag::getUserId, userId)
        );

        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 删除标签关联
        relationMapper.delete(
                new QueryWrapper<Object>().eq("tag_id", id)
        );

        // 删除标签
        tagMapper.deleteById(id);

        log.info("删除标签: id={}", id);
    }

    @Override
    @Transactional
    public void addTagsToPlan(Long planId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 批量插入关联
        String sql = "INSERT IGNORE INTO t_plan_tag_relation (plan_id, tag_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (Long tagId : tagIds) {
            batchArgs.add(new Object[]{planId, tagId});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("为任务添加标签: planId={}, tagCount={}", planId, tagIds.size());
    }

    @Override
    @Transactional
    public void removeTagsFromPlan(Long planId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        relationMapper.delete(
                new QueryWrapper<Object>()
                        .eq("plan_id", planId)
                        .in("tag_id", tagIds)
        );

        log.info("从任务移除标签: planId={}, tagCount={}", planId, tagIds.size());
    }

    @Override
    public List<TagResponse> getPlanTags(Long planId) {
        String sql = """
            SELECT t.* FROM t_plan_tag t
            INNER JOIN t_plan_tag_relation r ON t.id = r.tag_id
            WHERE r.plan_id = ?
            ORDER BY t.name
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TagResponse response = new TagResponse();
            response.setId(rs.getLong("id"));
            response.setUserId(rs.getLong("user_id"));
            response.setName(rs.getString("name"));
            response.setColor(rs.getString("color"));
            response.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            response.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return response;
        }, planId);
    }

    private TagResponse toResponse(PlanTag tag) {
        TagResponse response = new TagResponse();
        BeanUtils.copyProperties(tag, response);
        return response;
    }
}
