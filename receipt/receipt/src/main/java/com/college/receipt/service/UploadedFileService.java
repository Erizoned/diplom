package com.college.receipt.service;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.UploadedFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class UploadedFileService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    private static final Logger logger = LoggerFactory.getLogger(UploadedFileService.class);

    private final String FOLDER_PATH = System.getProperty("user.dir") + "/uploads";

    public UploadedFile uploadImageToDataSystem(MultipartFile file, Recipe recipe, String entityType) throws IOException {
        String contentType = file.getContentType();
        logger.debug("MIME Type: " + contentType);
        int fileSize = Math.toIntExact(file.getSize() / 1024);
        if ( fileSize > 2 * 1024){
            throw new IllegalArgumentException("Ошибка. Файл не должен весить больше 2 мегабайт. Вес файла: " + fileSize / 1024 + " мегабайт");
        }

        if (contentType != null && !contentType.startsWith("image")) {
            throw new IllegalArgumentException("Ошибка. Доступны только изображения");
        }
        UploadedFile existingFile = null;
        if (entityType.equals("PHOTOFOOD")){
            existingFile = uploadedFileRepository.findByRecipeAndIsPhotoFoodTrue(recipe);
        }
        if(existingFile != null){
            logger.info("Найден старый файл {}. Удаление...", existingFile.getFilePath());
            File oldFile = new File(existingFile.getFilePath());
            if(oldFile.exists()){
                if (oldFile.delete()){
                    logger.info("Старая фотография профиля была удалена");
                }
                else {
                    logger.error("Ошибка. Не вышло удалить старую фотографию профиля {}", existingFile.getFilePath());
                }
            }
            recipe.getPhotos().remove(existingFile);
            recipeRepository.save(recipe);
            uploadedFileRepository.delete(existingFile);
        }

        String recipeId = String.valueOf(recipe.getId());
        String recipeFolderPath = Paths.get(FOLDER_PATH, recipeId).toString();

        File recipeFolder = new File(recipeFolderPath);
        if(!recipeFolder.exists()){
            recipeFolder.mkdirs();
            logger.info("Создана директория: {}", FOLDER_PATH);
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = Paths.get(recipeFolderPath, fileName).toString();
        UploadedFile uploadedFile = null;
        if (entityType.equals("PHOTOFOOD")){
            logger.info("Логика для добавления заставки фотографии еды");
            uploadedFile = UploadedFile.builder()
                    .name(fileName)
                    .type(contentType)
                    .filePath(filePath)
                    .recipe(recipe)
                    .isPhotoFood(true)
                    .build();
        } else if (entityType.equals("STEPPHOTO")) {
            logger.info("Логика для добавления фотографии шага");
            uploadedFile = UploadedFile.builder()
                    .name(fileName)
                    .type(contentType)
                    .filePath(filePath)
                    .fileSize(fileSize)
                    .recipe(recipe)
                    .isPhotoFood(false)
                    .build();
        } else if (entityType.isEmpty()){
            logger.info("Сохранение обычного изображения");
            uploadedFile = UploadedFile.builder()
                    .name(fileName)
                    .type(contentType)
                    .filePath(filePath)
                    .fileSize(fileSize)
                    .build();
        }
        try {
            file.transferTo(new File(filePath));
        } catch (Exception e) {
            logger.error("Файл {} не найден на пути: {}, ошибка: {}", file.getOriginalFilename(), filePath, e.getMessage());
            throw new IOException(e);
        }

        logger.info("Файл {} был сохранён в: {}", file.getOriginalFilename(), filePath);
        assert uploadedFile != null;
        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);
        savedFile.setRecipe(recipe);
        return savedFile;
    }

    public byte[] downloadImageFromFileSystem(String file) throws IOException {
        Optional<UploadedFile> fileData = uploadedFileRepository.findByName(file);
        String filePath = fileData.get().getFilePath();

        byte[] images = Files.readAllBytes(new File(filePath).toPath());
        return images;
    }
}

