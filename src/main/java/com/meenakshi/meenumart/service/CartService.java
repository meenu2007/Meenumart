package com.meenakshi.meenumart.service;

import com.meenakshi.meenumart.dao.CartDAO;
import com.meenakshi.meenumart.dao.ProductDAO;
import com.meenakshi.meenumart.model.CartItem;
import com.meenakshi.meenumart.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (product.getStockQty() < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }

        Optional<CartItem> existing = cartDAO.findByUserAndProduct(userId, productId);
        if (existing.isPresent()) {
            cartDAO.updateQuantity(existing.get().getId(), existing.get().getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cartDAO.addItem(item);
        }
    }

    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        cartDAO.updateQuantity(cartItemId, quantity);
    }

    public void removeItem(Long cartItemId) {
        cartDAO.removeItem(cartItemId);
    }

    public List<CartItem> getCart(Long userId) {
        return cartDAO.findByUser(userId);
    }

    public BigDecimal getCartTotal(Long userId) {
        return cartDAO.findByUser(userId).stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
