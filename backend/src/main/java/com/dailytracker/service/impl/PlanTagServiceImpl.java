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
 * 计划标签服务实现类（Plan Tag Service Implementation）
 *
 * 【类设计说明】
 * 本类是 PlanTagService 接口的具体实现，负责每日计划模块的标签管理功能。
 * 包括标签的 CRUD、任务与标签的关联管理、以及通过原生 SQL 的 JOIN 查询。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J 日志记录器
 * @Service    - Spring 注解，标记为业务层 Bean
 * @RequiredArgsConstructor - Lombok 注解，通过构造器注入依赖
 *
 * 【核心设计】
 * - 唯一名称约束：同一用户下不允许创建同名标签
 * - INSERT IGNORE 策略：批量添加标签关联时使用 INSERT IGNORE 避免重复
 * - 原生 SQL 查询：getPlanTags 使用 JOIN 查询提高性能
 * - JdbcTemplate：用于执行原生 SQL 和批量操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanTagServiceImpl implements PlanTagService {

    /** 计划标签数据访问层 */
    private final PlanTagMapper tagMapper;
    /** 计划-标签关联数据访问层 */
    private final PlanTagRelationMapper relationMapper;
    /** Spring JDBC 模板（用于执行原生 SQL 和批量操作） */
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

    /**
     * PlanTag 实体转 TagResponse DTO（私有工具方法）
     *
     * @param tag 计划标签实体
     * @return TagResponse 响应 DTO
     */
    private TagResponse toResponse(PlanTag tag) {
        TagResponse response = new TagResponse();
        BeanUtils.copyProperties(tag, response);
        return response;
    }
}
