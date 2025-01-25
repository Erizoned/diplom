package com.college.receipt.entities;

import lombok.*;

import jakarta.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "uploaded_files")
@Getter
@Setter
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    private String filePath;

    public boolean isPhotoFood;

    private Integer stepNumber;

//    private int stepNumber;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Override
    public String toString() {
        return "UploadedFile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", filePath='" + filePath + '\'' +
                ", isPhotoFood=" + isPhotoFood +
                '}'; // Не включайте поле recipe, чтобы избежать рекурсии
    }

}
