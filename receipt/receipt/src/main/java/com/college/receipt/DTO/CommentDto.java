package com.college.receipt.DTO;

import com.college.receipt.entities.Comments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;

    private String content;

    private Integer likes;

    private Integer dislikes;

    private String author;

    public CommentDto(Comments comments) {
        this.id = comments.getId();
        this.author = comments.getAuthor().getUsername();
        this.content = comments.getContent();
        this.likes = comments.getLikes();
        this.dislikes = comments.getDislikes();
    }
}
