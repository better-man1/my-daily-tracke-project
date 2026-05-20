package com.dailytracker.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口（File Storage Service Interface）
 *
 * 【职责说明】
 * 本接口定义了文件上传和删除的通用契约，抽象了底层存储实现细节。
 * 系统中的文件存储（如头像、记账图片、摘录图片等）均通过此接口完成，
 * 上层业务代码不需要关心文件具体存储在本地磁盘、MinIO、阿里云 OSS 还是 AWS S3。
 *
 * 【设计模式】
 * - 策略模式（Strategy Pattern）：
 *   本接口定义了文件存储的策略接口，不同的实现类代表不同的存储策略。
 *   当前系统使用 MinioFileStorageServiceImpl 作为实现（基于 MinIO 对象存储）。
 *   如果未来需要切换到其他存储方案（如阿里云 OSS），只需新增一个实现类，
 *   通过 Spring 的依赖注入切换即可，无需修改业务代码。
 *
 * - 依赖倒置原则（DIP）：
 *   Controller 层和 Service 层依赖的是 FileStorageService 接口，
 *   而非具体的 MinioFileStorageServiceImpl 实现类。
 *   这使得存储方案可以在不修改业务代码的情况下灵活替换。
 *
 * 【扩展说明】
 * 如果需要支持多种存储方案并存（如根据文件类型选择不同的存储），
 * 可以使用 Spring 的 @Conditional 或 @Primary 注解来控制注入哪个实现。
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * 【业务流程】
     * 1. 检查存储桶是否存在，不存在则自动创建
     * 2. 生成唯一的文件名（使用 UUID 避免冲突）
     * 3. 按日期构建目录结构（如 avatar/2026/05/20/）
     * 4. 上传文件到对象存储
     * 5. 返回文件的公开访问 URL
     *
     * @param file 待上传的文件（Spring 的 MultipartFile 对象，由前端表单提交）
     * @param path 存储路径（包含目录前缀，如：avatar/2026/04）
     * @return 文件的访问URL（可直接通过 HTTP GET 访问）
     * @throws com.dailytracker.common.exception.BusinessException 上传失败时抛出异常
     */
    String uploadFile(MultipartFile file, String path);

    /**
     * 删除文件
     *
     * 【设计说明】
     * 根据文件的完整 URL 解析出对象名，然后调用对象存储的删除接口。
     * 删除失败时只记录日志，不抛出异常（避免因文件删除失败影响主业务流程）。
     * 这是一种"容错设计"——文件删除是次要操作，不应阻塞核心业务。
     *
     * @param fileUrl 文件的访问URL（由 uploadFile 方法返回的完整 URL）
     */
    void deleteFile(String fileUrl);
}
