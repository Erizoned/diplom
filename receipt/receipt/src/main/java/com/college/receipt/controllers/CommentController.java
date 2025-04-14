package com.college.receipt.controllers;

import com.college.receipt.entities.Comments;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.CommentsRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.CommentService;
import com.college.receipt.service.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CommentService commentsService;

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @PostMapping("/{id}/comment")
    private ResponseEntity<Comments> addComment(@RequestParam String content, @PathVariable("id") Long id){
        Comments comments = commentsService.createComment(content, id);
        logger.info("Комментарий {} для рецепта {} создан. Автор: {}", comments.getContent(), comments.getRecipe().getName(),comments.getAuthor());
        return ResponseEntity.ok().body(comments);
    }

    @PostMapping("/comment/{id}/like")
    private ResponseEntity<String> addLikeToComment(@PathVariable("id") Long id){
        Comments comment = commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        if (comment.getLikes() != null){
            comment.setLikes(comment.getLikes() + 1);
        }
        else {
            comment.setLikes(1);
        }
        commentsRepository.save(comment);
        logger.info("Лайк к комментарию автора {} добавлен. Общее количество лайков: {}", comment.getAuthor().getUsername(), comment.getLikes());

        return ResponseEntity.ok().body("Лайк добавлен");
    }

    @PostMapping("/comment/{id}/dislike")
    private ResponseEntity<String> addDislikeToComment(@PathVariable("id") Long id){
        Comments comment = commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        if (comment.getDislikes() != null){
            comment.setLikes(comment.getDislikes() + 1);
        }
        else {
            comment.setDislikes(1);
        }
        commentsRepository.save(comment);
        logger.info("Дизлайк к комментарию автора {} добавлен. Общее количество дизлайков: {}", comment.getAuthor().getUsername(), comment.getDislikes());

        return ResponseEntity.ok().body("Дизлайк добавлен");
    }
    @DeleteMapping("/comment/{id}")
    private ResponseEntity<String> deleteComment(@PathVariable("id") Long id){
        Comments comment = commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        commentsRepository.delete(comment);

        logger.info("Комментарий {} от рецепта {} удалён.", comment.getId(),comment.getRecipe().getName());

        return ResponseEntity.ok().body("Комментарий удалён");
    }
}
