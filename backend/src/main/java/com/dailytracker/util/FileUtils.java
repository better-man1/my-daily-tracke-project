package com.dailytracker.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件工具类 - 提供文件相关的常用操作方法
 *
 * 【类的用途】
 * 封装文件处理相关的通用功能，包括：
 * 1. 生成唯一文件名（防止文件名冲突）
 * 2. 获取文件扩展名
 * 3. 判断文件类型（图片/文档）
 * 4. 格式化文件大小为人类可读的字符串
 *
 * 【设计思想】
 * 1. 使用 Set 集合定义允许的文件类型白名单，便于快速查找
 * 2. UUID 生成唯一文件名，避免并发上传时的文件名冲突
 * 3. 工具类模式：私有构造函数 + 静态方法
 *
 * 【在架构中的位置】
 * 属于工具层，被文件上传相关的 Service 使用。
 * 配合 MinIO 等对象存储服务，处理文件上传前的预处理工作。
 *
 * 【相关技术知识点】
 * - UUID（Universally Unique Identifier）：128位的全局唯一标识符
 * - HashSet：基于哈希表的 Set 实现，查找时间复杂度 O(1)
 * - 文件扩展名白名单：安全措施，防止上传恶意文件（如 .exe、.sh）
 */
public class FileUtils {

    /**
     * 私有构造函数 - 禁止实例化工具类
     */
    private FileUtils() {}

    /**
     * 允许的图片文件后缀白名单
     * 包含常见的图片格式：jpg、jpeg、png、gif、webp、bmp
     * 用于上传时的文件类型校验，不在白名单中的图片格式会被拒绝
     *
     * HashSet 的选择理由：
     * - contains() 方法时间复杂度为 O(1)，适合频繁的查找操作
     * - 使用静态初始化块 + Arrays.asList 一次性初始化
     */
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp")
    );

    /**
     * 允许的文档文件后缀白名单
     * 包含常见的文档格式：pdf、doc、docx、xls、xlsx、txt、md
     */
    private static final Set<String> DOC_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "txt", "md")
    );

    /**
     * 生成唯一文件名（保留原始后缀）
     *
     * 【方法作用】
     * 使用 UUID 生成一个全局唯一的文件名，同时保留原始文件的扩展名。
     * 这样可以避免不同用户上传同名文件时的冲突问题。
     *
     * 【使用场景】
     * 用户上传头像、附件等文件时，服务端需要为文件生成一个唯一的存储名称。
     *
     * 【生成规则】
     * UUID 去掉连字符（32位十六进制字符串） + 原始扩展名
     * 例如：原始文件 "photo.jpg" -> "a1b2c3d4e5f6...xyz.jpg"
     *
     * @param originalFilename 原始文件名（含扩展名），如 "我的照片.jpg"
     * @return String 新的唯一文件名，如 "a1b2c3d4e5f67890.jpg"
     */
    public static String generateUniqueFileName(String originalFilename) {
        // 获取原始文件的扩展名
        String extension = getExtension(originalFilename);
        // UUID.randomUUID() 生成随机的 UUID（如 "550e8400-e29b-41d4-a716-446655440000"）
        // replace("-", "") 去掉连字符，得到 32 位的十六进制字符串
        // 如果有扩展名则追加 ".扩展名"，否则只有 UUID 字符串
        return UUID.randomUUID().toString().replace("-", "") +
                (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 获取文件扩展名（不含点号，统一为小写）
     *
     * 【方法作用】
     * 从文件名中提取扩展名部分，转换为小写以统一比较。
     *
     * 【注意事项】
     * - 返回的扩展名不含点号（"."），如 "jpg" 而非 ".jpg"
     * - 统一转为小写，避免 "JPG" 和 "jpg" 被当作不同格式
     * - 如果文件名没有扩展名，返回空字符串 ""
     *
     * @param filename 文件名，如 "photo.JPG" 或 "document"
     * @return String 扩展名，如 "jpg"；如果没有扩展名则返回 ""
     */
    public static String getExtension(String filename) {
        // 检查文件名是否为 null 或不包含点号
        if (filename == null || !filename.contains(".")) {
            return "";   // 无扩展名
        }
        // lastIndexOf(".") 找到最后一个点号的位置（处理 "file.name.jpg" 的情况）
        // substring(dotIndex + 1) 获取点号后面的部分
        // toLowerCase() 统一转为小写
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 判断文件是否为图片类型
     *
     * 【方法作用】
     * 根据文件扩展名判断文件是否属于图片格式。
     * 用于上传时的文件类型校验和分类处理。
     *
     * @param filename 文件名（含扩展名）
     * @return boolean true 表示是图片，false 表示不是图片
     */
    public static boolean isImage(String filename) {
        // 先获取扩展名，然后在白名单中查找
        // HashSet.contains() 时间复杂度 O(1)，非常高效
        return IMAGE_EXTENSIONS.contains(getExtension(filename));
    }

    /**
     * 判断文件是否为文档类型
     *
     * 【方法作用】
     * 根据文件扩展名判断文件是否属于文档格式。
     * 用于上传时的文件类型校验和分类处理。
     *
     * @param filename 文件名（含扩展名）
     * @return boolean true 表示是文档，false 表示不是文档
     */
    public static boolean isDocument(String filename) {
        return DOC_EXTENSIONS.contains(getExtension(filename));
    }

    /**
     * 格式化文件大小为人类可读的字符串
     *
     * 【方法作用】
     * 将字节数转换为带单位的可读字符串，自动选择合适的单位（B/KB/MB/GB）。
     *
     * 【换算关系】
     * 1 KB = 1024 B
     * 1 MB = 1024 KB = 1024 * 1024 B
     * 1 GB = 1024 MB = 1024 * 1024 * 1024 B
     *
     * 【示例】
     * - 500 -> "500 B"
     * - 1536 -> "1.5 KB"
     * - 1572864 -> "1.5 MB"
     * - 1610612736 -> "1.5 GB"
     *
     * @param bytes 文件大小（字节数）
     * @return String 格式化后的字符串，如 "1.5 MB"
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            // 小于 1 KB，直接显示字节数
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            // 小于 1 MB，转换为 KB（保留1位小数）
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            // 小于 1 GB，转换为 MB（保留1位小数）
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            // 大于等于 1 GB，转换为 GB（保留1位小数）
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
