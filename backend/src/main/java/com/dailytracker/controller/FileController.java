package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理接口
 */
@Tag(name = "文件管理", description = "通用文件上传/删除接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "通用文件上传")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(defaultValue = "common") String path) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        String fileUrl = fileStorageService.uploadFile(file, path);
        return Result.success("上传成功", fileUrl);
    }
}
