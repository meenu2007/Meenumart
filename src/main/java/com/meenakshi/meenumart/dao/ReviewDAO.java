package com.meenakshi.meenumart.dao;

import com.meenakshi.meenumart.model.Review;
import java.util.List;

public interface ReviewDAO {
    Review create(Review review);
    List<Review> findByProduct(Long productId);
}
