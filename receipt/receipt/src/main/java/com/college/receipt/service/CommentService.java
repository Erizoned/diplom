package com.college.receipt.service;

import com.college.receipt.entities.Comments;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.CommentsRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    public Comments createComment(String content, Long recipeId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName());
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("Рецепт не найден"));

        Comments comments = Comments.builder()
                .content(content)
                .author(user)
                .recipe(recipe)
                .build();

        comments.setAuthor(user);
        commentsRepository.save(comments);
        recipe.getComments().add(comments);
        recipeRepository.save(recipe);

        return comments;
    }
}
