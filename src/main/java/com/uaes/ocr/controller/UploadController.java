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
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/client")
public class UploadController {

    // 存储分块的临时目录
    private final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/upload_chunks/";
    // 存储合并后文件的目录
    private final String UPLOAD_DIR = System.getProperty("user.home") + "/uploads/";

    // 存储文件信息的映射：key=uploadId, value=文件名
    private final ConcurrentHashMap<String, UploadInfo> fileInfoMap = new ConcurrentHashMap<>();

    // 存储分块信息的内部类
    static class UploadInfo {
        private String originalFilename;
        private int totalChunks;
        private boolean[] chunkStatus;  // 用于跟踪哪些分块已上传

        public UploadInfo(String originalFilename, int totalChunks) {
            this.originalFilename = originalFilename;
            this.totalChunks = totalChunks;
            this.chunkStatus = new boolean[totalChunks];
        }

        public void setChunkReceived(int chunkIndex) {
            if (chunkIndex >= 0 && chunkIndex < chunkStatus.length) {
                chunkStatus[chunkIndex] = true;
            }
        }

        public boolean allChunksReceived() {
            for (boolean received : chunkStatus) {
                if (!received) return false;
            }
            return true;
        }
    }

    @PostMapping("/Upload")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("chunk") int chunkIndex,
            @RequestParam("total") int totalChunks,
            @RequestParam("id") String uploadId,
            @RequestParam("file") MultipartFile file) {

        // 确保目录存在
        new File(TEMP_DIR).mkdirs();
        new File(UPLOAD_DIR).mkdirs();

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();

        // 如果是第一个分块，存储文件信息
        if (chunkIndex == 1) {
            fileInfoMap.put(uploadId, new UploadInfo(originalFilename, totalChunks));
        }

        // 保存分块到临时目录
        // 注意：前端传递的 chunkIndex 是 1-based，我们转换为 0-based
        int zeroBasedChunkIndex = chunkIndex - 1;
        String chunkFileName = uploadId + "_" + zeroBasedChunkIndex + ".part";
        Path tempPath = Paths.get(TEMP_DIR + chunkFileName);

        try {
            // 保存分块文件
            Files.write(tempPath, file.getBytes());

            // 更新分块状态
            UploadInfo info = fileInfoMap.get(uploadId);
            if (info != null) {
                info.setChunkReceived(zeroBasedChunkIndex);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("chunkIndex", chunkIndex);
            response.put("status", "success");
            response.put("message", "Chunk " + chunkIndex + " of " + totalChunks + " uploaded successfully");

            // 如果是最后一个分块，触发合并操作
            if (chunkIndex == totalChunks) {
                boolean merged = mergeChunks(uploadId, totalChunks);
                if (merged) {
                    response.put("message", "All chunks received, file merged successfully");
                    UploadInfo uploadInfo = fileInfoMap.get(uploadId);
                    if (uploadInfo != null) {
                        response.put("filePath", UPLOAD_DIR + uploadInfo.originalFilename);
                    }
                } else {
                    response.put("message", "Failed to merge chunks");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to save chunk: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 合并所有分块
     */
    private boolean mergeChunks(String uploadId, int totalChunks) {
        try {
            // 获取文件信息
            UploadInfo uploadInfo = fileInfoMap.get(uploadId);
            if (uploadInfo == null) {
                throw new IOException("Upload info not found for ID: " + uploadId);
            }

            String originalFilename = uploadInfo.originalFilename;
            Path outputFile = Paths.get(UPLOAD_DIR + originalFilename);

            // 创建父目录（如果文件路径包含子目录）
            Files.createDirectories(outputFile.getParent());

            // 创建输出流
            try (var outputStream = Files.newOutputStream(outputFile)) {
                // 按顺序合并所有分块
                for (int i = 0; i < totalChunks; i++) {
                    String chunkFileName = uploadId + "_" + i + ".part";
                    Path chunkPath = Paths.get(TEMP_DIR + chunkFileName);

                    if (!Files.exists(chunkPath)) {
                        throw new IOException("Chunk file not found: " + chunkFileName);
                    }

                    // 将分块内容写入输出文件
                    Files.copy(chunkPath, outputStream);

                    // 删除分块文件
                    Files.delete(chunkPath);
                }
            }

            // 清理文件信息
            fileInfoMap.remove(uploadId);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查分块上传状态
     * 前端可以通过此接口检查哪些分块已上传
     */
    @GetMapping("/checkChunks")
    public ResponseEntity<Map<String, Object>> checkChunks(
            @RequestParam("id") String uploadId,
            @RequestParam("total") int totalChunks) {

        Map<String, Object> response = new HashMap<>();
        response.put("uploadId", uploadId);

        UploadInfo info = fileInfoMap.get(uploadId);
        if (info != null) {
            response.put("exists", true);
            response.put("filename", info.originalFilename);

            // 返回已上传的分块索引
            boolean[] uploadedChunks = new boolean[totalChunks];
            for (int i = 0; i < totalChunks; i++) {
                String chunkFileName = uploadId + "_" + i + ".part";
                Path chunkPath = Paths.get(TEMP_DIR + chunkFileName);
                uploadedChunks[i] = Files.exists(chunkPath);
            }
            response.put("uploadedChunks", uploadedChunks);
        } else {
            response.put("exists", false);

            // 检查是否有遗留的分块文件
            boolean[] existingChunks = new boolean[totalChunks];
            for (int i = 0; i < totalChunks; i++) {
                String chunkFileName = uploadId + "_" + i + ".part";
                Path chunkPath = Paths.get(TEMP_DIR + chunkFileName);
                existingChunks[i] = Files.exists(chunkPath);
            }
            response.put("existingChunks", existingChunks);
        }

        return ResponseEntity.ok(response);
    }
}