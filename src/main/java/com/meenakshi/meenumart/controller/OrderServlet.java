package com.meenakshi.meenumart.controller;

import com.google.gson.Gson;
import com.meenakshi.meenumart.dao.CartDAOImpl;
import com.meenakshi.meenumart.dao.OrderDAOImpl;
import com.meenakshi.meenumart.dao.ProductDAOImpl;
import com.meenakshi.meenumart.listener.DataSourceListener;
import com.meenakshi.meenumart.model.Order;
import com.meenakshi.meenumart.service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/orders")
public class OrderServlet extends HttpServlet {

    private OrderService orderService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        DataSource ds = (DataSource) getServletContext().getAttribute(DataSourceListener.DATASOURCE_ATTR);
        orderService = new OrderService(new OrderDAOImpl(ds), new CartDAOImpl(ds), new ProductDAOImpl(ds));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Login required");
            return;
        }
        Long userId = (Long) session.getAttribute("userId");
        List<Order> orders = orderService.getBuyerOrders(userId);
        writeSuccess(resp, HttpServletResponse.SC_OK, orders);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Login required");
            return;
        }
        Long userId = (Long) session.getAttribute("userId");

        try {
            Order created = orderService.placeOrder(userId);
            writeSuccess(resp, HttpServletResponse.SC_CREATED, created);
        } catch (IllegalStateException e) {
            writeError(resp, HttpServletResponse.SC_CONFLICT, "ORDER_FAILED", e.getMessage());
        }
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
