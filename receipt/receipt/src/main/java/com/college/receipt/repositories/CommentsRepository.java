package com.college.receipt.repositories;

import com.college.receipt.entities.Comments;
import com.college.receipt.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends CrudRepository<Comments, Long> {
    List<Comments> findByRecipe(Recipe recipe);
}
