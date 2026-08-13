package com.meenakshi.meenumart.dao;

import com.meenakshi.meenumart.model.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartDAO {
    CartItem addItem(CartItem item);
    Optional<CartItem> findByUserAndProduct(Long userId, Long productId);
    List<CartItem> findByUser(Long userId);
    void updateQuantity(Long cartItemId, int quantity);
    void removeItem(Long cartItemId);
    void clearCart(Long userId);
}
