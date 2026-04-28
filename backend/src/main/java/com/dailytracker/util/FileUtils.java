package com.dailytracker.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件工具类
 */
public class FileUtils {

    private FileUtils() {}

    /** 允许的图片后缀 */
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp")
    );

    /** 允许的文档后缀 */
    private static final Set<String> DOC_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "txt", "md")
    );

    /**
     * 生成唯一文件名（保留原始后缀）
     *
     * @param originalFilename 原始文件名
     * @return 新文件名
     */
    public static String generateUniqueFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") +
                (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 获取文件扩展名（不含点，小写）
     *
     * @param filename 文件名
     * @return 扩展名
     */
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 判断是否为图片
     *
     * @param filename 文件名
     * @return true 表示是图片
     */
    public static boolean isImage(String filename) {
        return IMAGE_EXTENSIONS.contains(getExtension(filename));
    }

    /**
     * 判断是否为文档
     *
     * @param filename 文件名
     * @return true 表示是文档
     */
    public static boolean isDocument(String filename) {
        return DOC_EXTENSIONS.contains(getExtension(filename));
    }

    /**
     * 格式化文件大小为可读字符串
     *
     * @param bytes 字节数
     * @return 格式化字符串，如 "1.5 MB"
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
