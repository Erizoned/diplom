package service;

import com.college.receipt.UploadedFile;
import com.college.receipt.UploadedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UploadedFileService {

    private final UploadedFileRepository fileRepository;

    public UploadedFileService(UploadedFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // Метод для сохранения файла
    public UploadedFile saveFile(MultipartFile file) throws IOException {
        UploadedFile uploadedFile = UploadedFile.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .data(file.getBytes())
                .build();
        return fileRepository.save(uploadedFile);
    }

    // Метод для получения файла по ID
    public Optional<UploadedFile> getFile(Long id) {
        return fileRepository.findById(id);
    }
}
