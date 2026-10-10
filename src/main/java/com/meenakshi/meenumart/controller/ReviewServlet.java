package com.meenakshi.meenumart.controller;

import com.google.gson.Gson;
import com.meenakshi.meenumart.dao.ReviewDAOImpl;
import com.meenakshi.meenumart.listener.DataSourceListener;
import com.meenakshi.meenumart.model.Review;
import com.meenakshi.meenumart.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/reviews")
public class ReviewServlet extends HttpServlet {

    private ReviewService reviewService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        DataSource ds = (DataSource) getServletContext().getAttribute(DataSourceListener.DATASOURCE_ATTR);
        reviewService = new ReviewService(new ReviewDAOImpl(ds));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String productIdParam = req.getParameter("productId");
        if (productIdParam == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "productId is required");
            return;
        }
        List<Review> reviews = reviewService.getReviewsForProduct(Long.parseLong(productIdParam));
        writeSuccess(resp, HttpServletResponse.SC_OK, reviews);
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
        Map<String, Object> body = gson.fromJson(req.getReader(), Map.class);

        try {
            Long productId = ((Double) body.get("productId")).longValue();
            int rating = ((Double) body.get("rating")).intValue();
            String comment = (String) body.get("comment");

            Review created = reviewService.addReview(productId, userId, rating, comment);
            writeSuccess(resp, HttpServletResponse.SC_CREATED, created);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
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
