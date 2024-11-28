package com.college.receipt.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Имя файла

    private String type; // MIME-тип файла

    private String filePath; // Содержимое файла

    public boolean isPhotoFood;

//    private int stepNumber;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
}
