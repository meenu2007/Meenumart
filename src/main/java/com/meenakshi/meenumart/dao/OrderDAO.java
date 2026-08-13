package com.meenakshi.meenumart.dao;

import com.meenakshi.meenumart.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Order createOrder(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByBuyer(Long buyerId);
    List<Order> findBySellerProducts(Long sellerId);
}
