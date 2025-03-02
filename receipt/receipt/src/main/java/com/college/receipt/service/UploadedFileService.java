package com.college.receipt.service;

import com.college.receipt.entities.UploadedFile;
import com.college.receipt.repositories.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Service
public class UploadedFileService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    private final String FOLDER_PATH = "C:/User/Anton/Documents/photos";

    public String uploadImageToDataSystem(MultipartFile file) throws IOException {
        String filePath = FOLDER_PATH + file.getOriginalFilename();
        UploadedFile uploadedFile = UploadedFile.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .filePath(filePath)
                .build();

        file.transferTo(new File(filePath));
        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);
        if(uploadedFile != null){
            return "Файл успешно загружен " + filePath;
        }
        return null;
    }

    public UploadedFile save(UploadedFile file) {
        return uploadedFileRepository.save(file);
    }

    public byte[] downloadImageFromFileSystem(String file) throws IOException {
        Optional<UploadedFile> fileData = uploadedFileRepository.findByName(file);
        String filePath = fileData.get().getFilePath();

        byte[] images = Files.readAllBytes(new File(filePath).toPath());
        return images;
    }
}

