package com.college.receipt.controllers;

import com.college.receipt.service.UploadedFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class UploadedFileController {

    private final UploadedFileService fileService;

    private static final Logger logger = LoggerFactory.getLogger(UploadedFileController.class);

    public UploadedFileController(UploadedFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/file_system")
    public ResponseEntity<?> uploadImageToFileSystem(@RequestParam("image")MultipartFile file) throws IOException{
        String uploadImage = fileService.uploadImageToDataSystem(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }

    @GetMapping("/file_system")
    public ResponseEntity<?> downloadImageFromFileSystem(@RequestParam("file_name") String fileName) throws IOException {
        logger.info("Запрос на получение изображения {}", fileName);
        byte[] imageData = fileService.downloadImageFromFileSystem(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);
    }
}
