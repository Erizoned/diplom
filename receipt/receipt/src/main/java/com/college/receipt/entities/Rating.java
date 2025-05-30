package com.college.receipt.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rating {
    @Column(nullable = false)
    private double rating = 0.0;

    @Column(nullable = false)
    private double votes = 0;

    public void updateRating(int newVote) {
        double totalScore = this.rating * this.votes;
        this.votes += 1;
        this.rating = (totalScore + newVote) / this.votes;
    }
}
