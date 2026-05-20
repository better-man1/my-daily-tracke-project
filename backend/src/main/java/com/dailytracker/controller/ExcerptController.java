package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
// IPage: MyBatis-Plus分页接口，封装分页查询结果
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.common.result.Result;
// ExcerptCreateRequest: 创建/更新摘录请求DTO，包含内容、来源、标签等字段
import com.dailytracker.dto.request.ExcerptCreateRequest;
// ExcerptResponse: 摘录响应DTO，包含摘录的完整信息
import com.dailytracker.dto.response.ExcerptResponse;
// ExcerptService: 摘录业务逻辑层接口
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
 * 每日摘录控制器（ExcerptController）
 *
 * 【职责说明】
 * 本控制器负责处理知识摘录/笔记管理相关的API请求，包括：
 *   1. 摘录的CRUD操作（创建、查询、更新、删除）
 *   2. 分页查询与多条件筛选（日期范围、来源类型、标签、收藏状态）
 *   3. 收藏/取消收藏的切换
 *   4. 随机获取一条摘录（每日推荐功能）
 *   5. 标签管理（获取所有标签、创建新标签）
 *   6. 全文搜索（基于关键词搜索摘录内容）
 *   7. 导出为Markdown格式（方便备份和迁移）
 *
 * 【RESTful设计思想 - 混合资源管理】
 * 本控制器管理两种相关但独立的资源：摘录（Excerpts）和标签（Tags）
 * 设计选择：将标签管理放在摘录控制器内（而非独立的TagController），因为：
 *   - 这里的标签是摘录专用的（不是全局标签）
 *   - 标签的CRUD操作简单，不值得单独创建一个Controller
 *   - 路径设计：/excerpts/tags 表示"摘录模块下的标签"
 *
 * 【API路径前缀】/api/v1/excerpts
 */
@Tag(name = "每日摘录", description = "摘录知识管理接口")
@RestController
@RequestMapping("/api/v1/excerpts")
@RequiredArgsConstructor
public class ExcerptController {

    private final ExcerptService excerptService;

    /**
     * 创建新的摘录
     *
     * 【HTTP方法】POST
     * 【URL路径】POST /api/v1/excerpts
     *
     * 【参数说明】
     * @param request 摘录创建请求DTO，包含：
     *                - content: 摘录内容（必填）
     *                - sourceType: 来源类型（BOOK/AUDIO/VIDEO/WEB/OTHER）
     *                - sourceTitle: 来源标题（如书名、视频标题）
     *                - sourceAuthor: 来源作者
     *                - tagIds: 关联的标签ID列表
     *                - isFavorite: 是否收藏
     *
     * 【返回值】Result<ExcerptResponse> - 创建成功后的摘录信息
     */
    @Operation(summary = "创建摘录")
    @PostMapping
    public Result<ExcerptResponse> create(@Valid @RequestBody ExcerptCreateRequest request) {
        return Result.success(excerptService.create(request));
    }

    /**
     * 分页查询摘录列表，支持多条件筛选
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/excerpts
     *
     * 【参数说明】
     * @param pageNum    页码（默认1）
     * @param pageSize   每页条数（默认20）
     * @param startDate  创建日期范围-开始（可选）
     * @param endDate    创建日期范围-结束（可选）
     * @param sourceType 来源类型筛选（可选），如 BOOK/AUDIO/VIDEO/WEB/OTHER
     * @param tagId      标签ID筛选（可选），只返回包含该标签的摘录
     * @param isFavorite 收藏筛选（可选），1=只看收藏，0=只看未收藏
     *
     * 【返回值】Result<IPage<ExcerptResponse>> - 分页结果
     *
     * 【设计说明】
     * - 提供丰富的筛选条件，方便用户快速定位摘录
     * - 使用分页而非全量返回：摘录可能积累很多（数百条），需要分页
     * - 所有筛选参数都是可选的，灵活组合
     *
     * 【请求示例】
     * GET /excerpts?pageNum=1&pageSize=10&sourceType=BOOK&isFavorite=1
     * 含义：获取第1页的收藏书籍摘录，每页10条
     */
    @Operation(summary = "摘录列表（分页 + 筛选）")
    @GetMapping
    public Result<IPage<ExcerptResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sourceType, // 来源类型筛选
            @RequestParam(required = false) Long tagId,        // 标签筛选
            @RequestParam(required = false) Integer isFavorite) { // 收藏筛选（1/0）
        return Result.success(excerptService.page(pageNum, pageSize, startDate, endDate, sourceType, tagId, isFavorite));
    }

    /**
     * 获取指定摘录的详细信息
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/excerpts/{id}
     */
    @Operation(summary = "摘录详情")
    @GetMapping("/{id}")
    public Result<ExcerptResponse> getById(@PathVariable Long id) {
        return Result.success(excerptService.getById(id));
    }

    /**
     * 更新指定摘录
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/excerpts/{id}
     */
    @Operation(summary = "更新摘录")
    @PutMapping("/{id}")
    public Result<ExcerptResponse> update(@PathVariable Long id,
                                          @Valid @RequestBody ExcerptCreateRequest request) {
        return Result.success(excerptService.update(id, request));
    }

    /**
     * 删除指定摘录
     *
     * 【HTTP方法】DELETE
     * 【URL路径】DELETE /api/v1/excerpts/{id}
     */
    @Operation(summary = "删除摘录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        excerptService.delete(id);
        return Result.success();
    }

    /**
     * 切换摘录的收藏状态
     *
     * 【HTTP方法】PUT - 更新摘录的收藏属性
     * 【URL路径】PUT /api/v1/excerpts/{id}/favorite
     *
     * 【参数说明】@param id 摘录ID
     * 【返回值】Result<Void> - 切换成功
     *
     * 【设计说明】
     * - "切换"而非"设置"：每次调用将收藏状态取反（收藏变未收藏，未收藏变收藏）
     * - 使用PUT方法：因为是在"更新"资源的某个属性
     * - 路径 /{id}/favorite 将"收藏状态"视为摘录的子资源
     * - 这种toggle设计简化了前端操作（不需要先查询当前状态再设置）
     */
    @Operation(summary = "切换收藏状态")
    @PutMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long id) {
        // Service层内部会查询当前收藏状态，然后取反保存
        excerptService.toggleFavorite(id);
        return Result.success();
    }

    /**
     * 随机获取一条摘录
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/excerpts/random
     *
     * 【参数说明】无需参数
     * 【返回值】Result<ExcerptResponse> - 随机一条摘录
     *
     * 【应用场景】
     * - "每日金句"功能：每次打开APP时显示一条随机摘录
     * - 温故知新：帮助用户回顾之前记录的内容
     *
     * 【实现说明】
     * - 数据库层面使用 ORDER BY RAND() LIMIT 1 实现随机选取
     * - 对于大量数据，可以考虑优化的随机算法（如基于ID范围的随机）
     */
    @Operation(summary = "随机获取一条摘录")
    @GetMapping("/random")
    public Result<ExcerptResponse> random() {
        return Result.success(excerptService.getRandom());
    }

    // ==================== 标签管理 ====================

    /**
     * 获取摘录模块的所有标签
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/excerpts/tags
     *
     * 【返回值】Result<List<Map<String, Object>>> - 标签列表
     *           每个标签包含：id、name、color、关联的摘录数量等
     *
     * 【设计说明】
     * - 路径 /excerpts/tags 表示"摘录模块下的标签"
     * - 与摘录列表路径 /excerpts 不冲突（通过 /tags 后缀区分）
     */
    @Operation(summary = "获取所有标签")
    @GetMapping("/tags")
    public Result<List<Map<String, Object>>> getAllTags() {
        return Result.success(excerptService.getAllTags());
    }

    /**
     * 创建新标签
     *
     * 【HTTP方法】POST
     * 【URL路径】POST /api/v1/excerpts/tags
     *
     * 【参数说明】
     * @param name  标签名称（必填），如"哲学"、" productivity"
     * @param color 标签颜色（可选），如"#FF5722"，用于前端标签的颜色展示
     *
     * 【返回值】Result<Map<String, Object>> - 创建的标签信息
     *
     * 【设计说明】
     * - 使用查询参数而非请求体：因为只有两个字段，且都是简单类型
     * - 更规范的做法是创建TagCreateRequest DTO + @RequestBody
     */
    @Operation(summary = "创建标签")
    @PostMapping("/tags")
    public Result<Map<String, Object>> createTag(
            @RequestParam String name,                          // 标签名称（必填）
            @RequestParam(required = false) String color) {     // 标签颜色（可选）
        return Result.success(excerptService.createTag(name, color));
    }

    // ==================== 搜索与导出 ====================

    /**
     * 全文搜索摘录
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/excerpts/search
     *
     * 【参数说明】
     * @param keyword  搜索关键词（必填），在摘录内容、来源标题、备注中搜索
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认20）
     *
     * 【返回值】Result<IPage<ExcerptResponse>> - 搜索结果（分页）
     *
     * 【设计说明】
     * - 独立的搜索路径 /search：与列表查询 GET /excerpts 分离
     *   因为搜索的逻辑和参数与普通列表查询不同
     * - 关键词搜索通常使用 LIKE '%keyword%' 或全文索引实现
     * - 对于大量数据，建议使用Elasticsearch等专业搜索引擎
     *
     * 【请求示例】GET /excerpts/search?keyword=时间管理&pageNum=1&pageSize=10
     */
    @Operation(summary = "全文搜索")
    @GetMapping("/search")
    public Result<IPage<ExcerptResponse>> search(
            @RequestParam String keyword,                        // 搜索关键词（必填）
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(excerptService.search(keyword, pageNum, pageSize));
    }

    /**
     * 将所有摘录导出为Markdown格式
     *
     * 【HTTP方法】GET - 获取导出的数据（只读操作）
     * 【URL路径】GET /api/v1/excerpts/export-markdown
     *
     * 【参数说明】无需参数，自动导出当前用户的所有摘录
     *
     * 【返回值】Result<String> - Markdown格式的文本内容
     *
     * 【设计说明】
     * - 使用GET方法：导出是只读操作
     * - 返回String而非文件下载：简化实现，前端可以将文本保存为.md文件
     * - Markdown格式：通用性强，可导入到Obsidian、Notion等笔记工具
     *
     * 【安全说明】
     * - 使用SecurityUtils.getCurrentUserId()获取当前用户ID
     * - 只导出当前用户的摘录，不会泄露他人数据
     */
    @Operation(summary = "导出为 Markdown")
    @GetMapping("/export-markdown")
    public Result<String> exportMarkdown() {
        // 获取当前登录用户ID，确保只导出该用户自己的摘录
        Long userId = com.dailytracker.util.SecurityUtils.getCurrentUserId();
        return Result.success(excerptService.exportMarkdown(userId));
    }
}
