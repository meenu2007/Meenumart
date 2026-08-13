package com.meenakshi.meenumart.service;

import com.meenakshi.meenumart.dao.ProductDAO;
import com.meenakshi.meenumart.model.Product;
import com.meenakshi.meenumart.util.ValidationUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Product createListing(Long sellerId, String name, String description,
                                  BigDecimal price, int stockQty, String category, String imageUrl) {
        if (!ValidationUtil.isNonEmpty(name)) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (stockQty < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        Product p = new Product();
        p.setSellerId(sellerId);
        p.setName(name.trim());
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQty(stockQty);
        p.setCategory(category);
        p.setImageUrl(imageUrl);
        return productDAO.create(p);
    }

    public List<Product> search(String keyword, String category) {
        return productDAO.search(keyword, category);
    }

    public Optional<Product> findById(Long id) {
        return productDAO.findById(id);
    }

    public List<Product> findBySeller(Long sellerId) {
        return productDAO.findBySeller(sellerId);
    }

    public void delete(Long productId, Long requestingUserId, boolean isAdmin) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!isAdmin && !product.getSellerId().equals(requestingUserId)) {
            throw new SecurityException("Not authorized to delete this product");
        }
        productDAO.delete(productId);
    }
}
