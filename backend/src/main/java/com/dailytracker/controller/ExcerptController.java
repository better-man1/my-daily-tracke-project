package com.dailytracker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.ExcerptCreateRequest;
import com.dailytracker.dto.response.ExcerptResponse;
import com.dailytracker.service.ExcerptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日摘录控制器
 */
@Tag(name = "每日摘录", description = "摘录知识管理接口")
@RestController
@RequestMapping("/api/v1/excerpts")
@RequiredArgsConstructor
public class ExcerptController {

    private final ExcerptService excerptService;

    @Operation(summary = "创建摘录")
    @PostMapping
    public Result<ExcerptResponse> create(@Valid @RequestBody ExcerptCreateRequest request) {
        return Result.success(excerptService.create(request));
    }

    @Operation(summary = "摘录列表（分页 + 筛选）")
    @GetMapping
    public Result<IPage<ExcerptResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Integer isFavorite) {
        return Result.success(excerptService.page(pageNum, pageSize, startDate, endDate, sourceType, tagId, isFavorite));
    }

    @Operation(summary = "摘录详情")
    @GetMapping("/{id}")
    public Result<ExcerptResponse> getById(@PathVariable Long id) {
        return Result.success(excerptService.getById(id));
    }

    @Operation(summary = "更新摘录")
    @PutMapping("/{id}")
    public Result<ExcerptResponse> update(@PathVariable Long id,
                                          @Valid @RequestBody ExcerptCreateRequest request) {
        return Result.success(excerptService.update(id, request));
    }

    @Operation(summary = "删除摘录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        excerptService.delete(id);
        return Result.success();
    }

    @Operation(summary = "切换收藏状态")
    @PutMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long id) {
        excerptService.toggleFavorite(id);
        return Result.success();
    }

    @Operation(summary = "随机获取一条摘录")
    @GetMapping("/random")
    public Result<ExcerptResponse> random() {
        return Result.success(excerptService.getRandom());
    }

    @Operation(summary = "获取所有标签")
    @GetMapping("/tags")
    public Result<List<Map<String, Object>>> getAllTags() {
        return Result.success(excerptService.getAllTags());
    }

    @Operation(summary = "创建标签")
    @PostMapping("/tags")
    public Result<Map<String, Object>> createTag(@RequestParam String name, @RequestParam(required = false) String color) {
        return Result.success(excerptService.createTag(name, color));
    }

    @Operation(summary = "全文搜索")
    @GetMapping("/search")
    public Result<IPage<ExcerptResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(excerptService.search(keyword, pageNum, pageSize));
    }

    @Operation(summary = "导出为 Markdown")
    @GetMapping("/export-markdown")
    public Result<String> exportMarkdown() {
        Long userId = com.dailytracker.util.SecurityUtils.getCurrentUserId();
        return Result.success(excerptService.exportMarkdown(userId));
    }
}
