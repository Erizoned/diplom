package com.college.receipt.controllers;

import com.college.receipt.service.UploadedFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class UploadedFileController {

    private final UploadedFileService fileService;

    public UploadedFileController(UploadedFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/file_system")
    public ResponseEntity<?> uploadImageToFileSystem(@RequestParam("image")MultipartFile file) throws IOException{
        String uploadImage = fileService.uploadImageToDataSystem(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }

    @GetMapping("/file_system/{file_name}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String file_name) throws IOException {
        byte[] imageData = fileService.downloadImageFromFileSystem(file_name);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);
    }
}
