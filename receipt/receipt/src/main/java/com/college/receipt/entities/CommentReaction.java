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
    private Comments comment;

    private boolean isLiked;
}
