package controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service.UploadedFileService;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class UploadedFileController {

    private final UploadedFileService fileService;

    public UploadedFileController(UploadedFileService fileService) {
        this.fileService = fileService;
    }

    // Загрузка файла
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            fileService.saveFile(file);
            return ResponseEntity.ok("Файл успешно загружен: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при загрузке файла");
        }
    }

    // Получение файла по ID
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {
        return fileService.getFile(id)
                .map(file -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                        .contentType(MediaType.valueOf(file.getType()))
                        .body(file.getData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
