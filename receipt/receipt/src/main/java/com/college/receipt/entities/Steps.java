package com.college.receipt.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "steps")
public class Steps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer stepNumber;

    private String description;

    @ManyToOne
    @JoinColumn(name = "photo_id", nullable = true)
    private UploadedFile photo;

    @JsonIgnore
    @ManyToOne
    // Поменять в базе данных на recipe_id
    @JoinColumn(name = "recip_id", nullable = false)
    private Recipe recipe;
}
