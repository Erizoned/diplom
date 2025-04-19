package com.college.receipt.service;

import com.college.receipt.entities.CommentReaction;
import com.college.receipt.entities.Comments;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.CommentReactionRepository;
import com.college.receipt.repositories.CommentsRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private CommentReactionRepository commentReactionRepository;

    @Autowired
    private UserService userService;

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

    public void likeComment(Comments comment) {
        User user = userService.findAuthenticatedUser();
        CommentReaction reaction = commentReactionRepository.findByUserAndComment(comment, user);
        logger.info("Ставится лайк. Текущее состояние: Лайков: {}. Дизлайков: {}", comment.getLikes(), comment.getDislikes());
        if (reaction != null) {
            if (reaction.isLiked()){
                comment.setLikes(comment.getLikes() - 1);
                commentReactionRepository.delete(reaction);
                commentsRepository.save(comment);
                logger.info("Пользователь уже поставил лайк, поэтому он удаляется");
            }
            else {
                comment.setLikes(comment.getLikes() + 1);
                comment.setDislikes(comment.getDislikes() - 1);
                reaction.setLiked(true);
                commentReactionRepository.save(reaction);
                commentsRepository.save(comment);
                logger.info("Поставлен лайк вместо дизлайка");
            }
        }
        else {
            CommentReaction newReaction = new CommentReaction();
            newReaction.setUser(user);
            newReaction.setComment(comment);
            newReaction.setLiked(true);
            commentReactionRepository.save(newReaction);
            comment.setLikes(comment.getLikes() + 1);
            commentsRepository.save(comment);
            logger.info("Лайк ставится впервые");
        }
    }

    public void dislikeComment(Comments comment) {
        User user = userService.findAuthenticatedUser();
        CommentReaction reaction = commentReactionRepository.findByUserAndComment(comment, user);
        logger.info("Ставится дизлайк. Текущее состояние: Лайков: {}. Дизлайков: {}", comment.getLikes(), comment.getDislikes());
        if (reaction != null) {
            if (!reaction.isLiked()){
                comment.setDislikes(comment.getDislikes() - 1);
                commentReactionRepository.delete(reaction);
                logger.info("Пользователь уже поставил дизлайк, поэтому он удаляется");
            }
            else {
                comment.setDislikes(comment.getDislikes() + 1);
                comment.setLikes(comment.getLikes() - 1);
                reaction.setLiked(false);
                commentReactionRepository.save(reaction);
                logger.info("Поставлен дизлайк вместо лайка.");
            }
        }
        else {
            logger.info("Дизлайк ставится впервые.");
            CommentReaction newReaction = new CommentReaction();
            newReaction.setUser(user);
            newReaction.setComment(comment);
            newReaction.setLiked(false);
            commentReactionRepository.save(newReaction);
            comment.setDislikes(comment.getDislikes() + 1);
        }
        commentsRepository.save(comment);
    }
}
