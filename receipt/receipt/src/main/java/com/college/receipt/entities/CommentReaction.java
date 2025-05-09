package com.college.receipt.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CommentReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    @JoinColumn(name= "comment_id", nullable = false)
    private Comments comment;

    private boolean isLiked;
}
