package com.college.receipt.controllers;

import com.college.receipt.DTO.CommentDto;
import com.college.receipt.entities.CommentReaction;
import com.college.receipt.entities.Comments;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.CommentReactionRepository;
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

import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private CommentReactionRepository commentReactionRepository;

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @GetMapping("/{id}/comment")
    private ResponseEntity<Comments> getComment(@PathVariable("id") Long id){
        Comments comments = commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        return ResponseEntity.ok().body(comments);
    }

    @PostMapping("/recipe/{id}/comment")
    private ResponseEntity<Comments> addComment(@RequestBody CommentDto commentDto, @PathVariable("id") Long id){
        String content = commentDto.getContent();
        Comments comments = commentsService.createComment(content, id);
        logger.info("Комментарий {} для рецепта {} создан. Автор: {}", comments.getContent(), comments.getRecipe().getName(),comments.getAuthor());
        return ResponseEntity.ok().body(comments);
    }

    @PostMapping("/comment/{id}/like")
    private ResponseEntity<?> addLikeToComment(@PathVariable("id") Long id){
        Comments comment = commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        commentsService.likeComment(comment);
        logger.info("Лайк к комментарию автора {} добавлен. Общее количество лайков: {}", comment.getAuthor().getUsername(), comment.getLikes());
        Map<String, Object> result = new HashMap<>();
        result.put("likes", comment.getLikes());
        result.put("dislikes", comment.getDislikes());
        result.put("reaction", "liked");
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/comment/{id}/dislike")
    private ResponseEntity<?> addDislikeToComment(@PathVariable("id") Long id){
        Comments comment = commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        commentsService.dislikeComment(comment);
        logger.info("Дизлайк к комментарию автора {} добавлен. Общее количество дизлайков: {}", comment.getAuthor().getUsername(), comment.getDislikes());
        Map<String, Object> result = new HashMap<>();
        result.put("likes", comment.getLikes());
        result.put("dislikes", comment.getDislikes());
        result.put("reaction", "disliked");
        return ResponseEntity.ok().body(result);
    }
    @DeleteMapping("/comment/{id}")
    private ResponseEntity<String> deleteComment(@PathVariable("id") Long id){
        Comments comment = commentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        commentsRepository.delete(comment);

        logger.info("Комментарий {} от рецепта {} удалён.", comment.getId(),comment.getRecipe().getName());

        return ResponseEntity.ok().body("Комментарий удалён");
    }
}
