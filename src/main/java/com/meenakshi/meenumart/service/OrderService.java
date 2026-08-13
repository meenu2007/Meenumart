package com.meenakshi.meenumart.service;

import com.meenakshi.meenumart.dao.CartDAO;
import com.meenakshi.meenumart.dao.OrderDAO;
import com.meenakshi.meenumart.dao.ProductDAO;
import com.meenakshi.meenumart.model.CartItem;
import com.meenakshi.meenumart.model.Order;
import com.meenakshi.meenumart.model.OrderItem;
import com.meenakshi.meenumart.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public OrderService(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    /** Places an order from the buyer's current cart via a mock payment confirmation. */
    public Order placeOrder(Long buyerId) {
        List<CartItem> cartItems = cartDAO.findByUser(buyerId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = productDAO.findById(cartItem.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Product no longer available"));
            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setQuantity(cartItem.getQuantity());
            oi.setUnitPrice(product.getPrice());
            orderItems.add(oi);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // Mock payment confirmation — no external gateway per spec scope constraints
        boolean paymentConfirmed = true;
        if (!paymentConfirmed) {
            throw new IllegalStateException("Payment failed");
        }

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus(Order.Status.CONFIRMED);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order created = orderDAO.createOrder(order);

        for (OrderItem oi : orderItems) {
            Product product = productDAO.findById(oi.getProductId()).get();
            productDAO.updateStock(product.getId(), product.getStockQty() - oi.getQuantity());
        }

        cartDAO.clearCart(buyerId);
        return created;
    }

    public List<Order> getBuyerOrders(Long buyerId) {
        return orderDAO.findByBuyer(buyerId);
    }

    public List<Order> getSellerOrders(Long sellerId) {
        return orderDAO.findBySellerProducts(sellerId);
    }
}
