package com.meenakshi.meenumart.dao;

import com.meenakshi.meenumart.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    Product create(Product product);
    Optional<Product> findById(Long id);
    List<Product> search(String keyword, String category);
    List<Product> findBySeller(Long sellerId);
    void updateStock(Long productId, int newStockQty);
    void delete(Long id);
}
