package com.college.receipt.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedRecipeDto {
    private Long id;
    private String name;
    private String description;
    private String avatar;
}
