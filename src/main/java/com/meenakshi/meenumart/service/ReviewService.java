package com.meenakshi.meenumart.service;

import com.meenakshi.meenumart.dao.ReviewDAO;
import com.meenakshi.meenumart.model.Review;
import java.util.List;

public class ReviewService {

    private final ReviewDAO reviewDAO;

    public ReviewService(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    public Review addReview(Long productId, Long userId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setComment(comment);
        return reviewDAO.create(review);
    }

    public List<Review> getReviewsForProduct(Long productId) {
        return reviewDAO.findByProduct(productId);
    }
}
