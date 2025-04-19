package com.college.receipt.repositories;

import com.college.receipt.entities.CommentReaction;
import com.college.receipt.entities.Comments;
import com.college.receipt.entities.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    @Query("SELECT r FROM CommentReaction r WHERE r.user = :user AND r.comment = :comment")
    CommentReaction findByUserAndComment(@Param("comment") Comments comment, @Param("user") User user);
}
