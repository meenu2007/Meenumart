package com.meenakshi.meenumart.dao;

import com.meenakshi.meenumart.model.CartItem;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartDAOImpl implements CartDAO {

    private final DataSource dataSource;

    public CartDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public CartItem addItem(CartItem item) {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getUserId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getLong(1));
            }
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add cart item", e);
        }
    }

    @Override
    public Optional<CartItem> findByUserAndProduct(Long userId, Long productId) {
        String sql = "SELECT * FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cart item", e);
        }
    }

    @Override
    public List<CartItem> findByUser(Long userId) {
        String sql = "SELECT ci.*, p.name AS product_name, p.price AS unit_price " +
                "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                "WHERE ci.user_id = ? ORDER BY ci.created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CartItem> results = new ArrayList<>();
                while (rs.next()) {
                    CartItem item = mapRow(rs);
                    item.setProductName(rs.getString("product_name"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    results.add(item);
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cart items", e);
        }
    }

    @Override
    public void updateQuantity(Long cartItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update cart quantity", e);
        }
    }

    @Override
    public void removeItem(Long cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove cart item", e);
        }
    }

    @Override
    public void clearCart(Long userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear cart", e);
        }
    }

    private CartItem mapRow(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return item;
    }
}
