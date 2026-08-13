package com.meenakshi.meenumart.controller;

import com.google.gson.Gson;
import com.meenakshi.meenumart.dao.ProductDAOImpl;
import com.meenakshi.meenumart.listener.DataSourceListener;
import com.meenakshi.meenumart.model.Product;
import com.meenakshi.meenumart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/products")
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        DataSource ds = (DataSource) getServletContext().getAttribute(DataSourceListener.DATASOURCE_ATTR);
        productService = new ProductService(new ProductDAOImpl(ds));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String keyword = req.getParameter("keyword");
        String category = req.getParameter("category");

        List<Product> results = productService.search(keyword, category);
        writeSuccess(resp, HttpServletResponse.SC_OK, results);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Login required");
            return;
        }

        Long sellerId = (Long) session.getAttribute("userId");
        Map<String, Object> body = gson.fromJson(req.getReader(), Map.class);

        try {
            Product created = productService.createListing(
                    sellerId,
                    (String) body.get("name"),
                    (String) body.get("description"),
                    new BigDecimal(body.get("price").toString()),
                    ((Double) body.get("stockQty")).intValue(),
                    (String) body.get("category"),
                    (String) body.get("imageUrl")
            );
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
