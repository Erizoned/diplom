package com.college.receipt.repositories;

import com.college.receipt.entities.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Integer> {
    Optional<UploadedFile> findByName(String file);
    Optional<UploadedFile> findById(Long id);
}
