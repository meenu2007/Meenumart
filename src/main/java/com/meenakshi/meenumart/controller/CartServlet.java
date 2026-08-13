package com.meenakshi.meenumart.controller;

import com.google.gson.Gson;
import com.meenakshi.meenumart.dao.CartDAOImpl;
import com.meenakshi.meenumart.dao.ProductDAOImpl;
import com.meenakshi.meenumart.listener.DataSourceListener;
import com.meenakshi.meenumart.model.CartItem;
import com.meenakshi.meenumart.service.CartService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/cart")
public class CartServlet extends HttpServlet {

    private CartService cartService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        DataSource ds = (DataSource) getServletContext().getAttribute(DataSourceListener.DATASOURCE_ATTR);
        cartService = new CartService(new CartDAOImpl(ds), new ProductDAOImpl(ds));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = requireUser(req, resp);
        if (userId == null) return;

        List<CartItem> items = cartService.getCart(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("total", cartService.getCartTotal(userId));
        writeSuccess(resp, HttpServletResponse.SC_OK, data);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = requireUser(req, resp);
        if (userId == null) return;

        Map<String, Object> body = gson.fromJson(req.getReader(), Map.class);
        try {
            Long productId = ((Double) body.get("productId")).longValue();
            int quantity = ((Double) body.get("quantity")).intValue();
            cartService.addToCart(userId, productId, quantity);
            writeSuccess(resp, HttpServletResponse.SC_CREATED, null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = requireUser(req, resp);
        if (userId == null) return;

        Map<String, Object> body = gson.fromJson(req.getReader(), Map.class);
        try {
            Long cartItemId = ((Double) body.get("cartItemId")).longValue();
            int quantity = ((Double) body.get("quantity")).intValue();
            cartService.updateQuantity(userId, cartItemId, quantity);
            writeSuccess(resp, HttpServletResponse.SC_OK, null);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Long userId = requireUser(req, resp);
        if (userId == null) return;

        String cartItemIdParam = req.getParameter("cartItemId");
        cartService.removeItem(Long.parseLong(cartItemIdParam));
        writeSuccess(resp, HttpServletResponse.SC_OK, null);
    }

    private Long requireUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Login required");
            return null;
        }
        return (Long) session.getAttribute("userId");
    }

    private void writeSuccess(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("success", true);
        envelope.put("data", data);
        envelope.put("error", null);
        resp.getWriter().write(gson.toJson(envelope));
    }

    private void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("success", false);
        envelope.put("data", null);
        envelope.put("error", error);
        resp.getWriter().write(gson.toJson(envelope));
    }
}
