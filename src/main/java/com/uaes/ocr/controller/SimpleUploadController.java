package com.uaes.ocr.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/client")
public class SimpleUploadController {

    // 上传文件存储目录
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "请选择要上传的文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 创建上传目录（如果不存在）
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();

            // 生成唯一的文件名，防止覆盖
            String fileName = UUID.randomUUID().toString() +
                    originalFilename.substring(originalFilename.lastIndexOf("."));

            // 完整的文件路径
            Path filePath = Paths.get(uploadDir + fileName);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 返回成功响应
            response.put("status", "success");
            response.put("message", "文件上传成功");
            response.put("filename", fileName);
            response.put("originalFilename", originalFilename);
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("url", "/uploads/" + fileName); // 文件访问URL

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

//// 简单文件上传
//async function uploadFile(file) {
//    const formData = new FormData();
//    formData.append('file', file);
//
//    try {
//        const response = await fetch('http://localhost:8080/client/upload', {
//                method: 'POST',
//                body: formData
//        });
//
//        const result = await response.json();
//        console.log('上传结果:', result);
//        return result;
//    } catch (error) {
//        console.error('上传失败:', error);
//    }
//}