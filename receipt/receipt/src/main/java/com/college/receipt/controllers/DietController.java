package com.college.receipt.controllers;

import com.college.receipt.entities.Diet;
import com.college.receipt.repositories.DietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diet")
public class DietController {

    @Autowired
    private DietRepository dietRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Diet> showDiet(@PathVariable("id") Long id){
        return ResponseEntity.ok().body(dietRepository.findById(id).orElseThrow(() -> new RuntimeException("Диета не найдена")));
    }
}
