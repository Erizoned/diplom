package com.college.receipt.DTO;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietDto {
    private Long id;
    private String recomendation;
    private LocalDate term;
    private LocalDate dateOfCreation;
}
