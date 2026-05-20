package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
import com.dailytracker.common.result.Result;
// FileStorageService: 文件存储服务接口，封装了文件上传到云存储（如阿里云OSS、腾讯云COS）的逻辑
import com.dailytracker.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
// MultipartFile: Spring提供的文件上传封装接口
// 封装了上传文件的字节内容、原始文件名、文件大小、Content-Type等信息
// 前端必须使用 multipart/form-data 编码格式提交包含此文件的表单
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器（FileController）
 *
 * 【职责说明】
 * 本控制器提供通用的文件上传接口，供多个模块使用：
 *   - 用户头像上传（也可以通过UserController的专用接口）
 *   - 摘录附件上传
 *   - 其他需要文件上传的场景
 *
 * 【RESTful设计思想 - 通用服务接口】
 * 与其他业务Controller不同，FileController提供的是"基础设施"级别的服务：
 *   - 不绑定特定业务实体（不像AccountingController绑定到"账目"）
 *   - 提供通用的文件存储能力，任何模块都可以调用
 *   - 通过path参数区分不同模块的文件存储路径
 *
 * 【文件上传安全最佳实践】
 *   1. 限制文件大小：通过Spring配置 spring.servlet.multipart.max-file-size 限制（如最大10MB）
 *   2. 校验文件类型：只允许上传安全的文件类型（jpg/png/gif/pdf等），禁止可执行文件
 *   3. 生成唯一文件名：使用UUID重命名文件，防止文件名冲突和路径遍历攻击
 *   4. 文件存储与Web服务器分离：文件存储在对象存储（OSS）而非应用服务器，提高安全性
 *   5. 不暴露服务器真实路径：返回的URL是CDN/ OSS的公网访问地址
 *
 * 【API路径前缀】/api/v1/files
 */
@Tag(name = "文件管理", description = "通用文件上传/删除接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 通用文件上传接口
     *
     * 【HTTP方法】POST - 上传（创建）新文件资源
     * 【URL路径】POST /api/v1/files/upload
     *
     * 【参数说明】
     * @param file 上传的文件（必填）
     *             @RequestParam("file") 从multipart/form-data请求中获取名为"file"的文件部分
     *             前端必须使用 enctype="multipart/form-data" 的表单提交
     *             前端代码示例：
     *             const formData = new FormData();
     *             formData.append('file', selectedFile);
     *             formData.append('path', 'avatar');
     *             fetch('/api/v1/files/upload', { method: 'POST', body: formData })
     *
     * @param path 存储路径分类（可选，默认"common"）
     *             用于区分不同模块的文件，如：
     *             - "avatar": 头像文件
     *             - "excerpt": 摘录附件
     *             - "common": 通用文件
     *             文件最终存储路径：{path}/{uuid}.{ext}
     *             例如：avatar/550e8400-e29b-41d4-a716-446655440000.jpg
     *
     * 【返回值】Result<String> - 上传成功后返回文件的公网访问URL
     *           例如：https://cdn.example.com/avatar/550e8400-xxxx.jpg
     *
     * 【文件上传流程】
     * 1. 前端选择文件，通过multipart/form-data提交
     * 2. Spring MVC自动将文件内容解析为MultipartFile对象
     * 3. Controller校验文件是否为空
     * 4. 调用FileStorageService上传文件到云存储
     * 5. 返回文件的访问URL给前端
     *
     * 【multipart/form-data vs application/json】
     * - 文件上传必须使用multipart/form-data格式（HTTP协议规定）
     * - 普通数据接口使用application/json格式
     * - multipart/form-data将表单数据分为多个"部分（part）"，每个部分可以包含文件或普通字段
     */
    @Operation(summary = "通用文件上传")
    @PostMapping("/upload")
    public Result<String> upload(
            // @RequestParam("file"): 从multipart请求中获取名为"file"的文件部分
            // "file"必须与前端FormData中append的key一致
            @RequestParam("file") MultipartFile file,
            // @RequestParam(defaultValue = "common"): 存储路径分类，默认为"common"
            @RequestParam(defaultValue = "common") String path) {
        // 校验文件是否为空：用户可能未选择文件就提交了表单
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        // 调用文件存储服务上传文件
        // Service层内部处理：
        //   1. 生成UUID文件名（防止冲突和安全问题）
        //   2. 校验文件类型（白名单机制）
        //   3. 上传到对象存储（如阿里云OSS）
        //   4. 返回文件的公网访问URL
        String fileUrl = fileStorageService.uploadFile(file, path);
        return Result.success("上传成功", fileUrl);
    }
}
