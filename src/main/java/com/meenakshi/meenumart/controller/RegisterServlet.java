package com.meenakshi.meenumart.controller;

import com.google.gson.Gson;
import com.meenakshi.meenumart.dao.UserDAOImpl;
import com.meenakshi.meenumart.listener.DataSourceListener;
import com.meenakshi.meenumart.model.User;
import com.meenakshi.meenumart.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/v1/auth/register")
public class RegisterServlet extends HttpServlet {

    private UserService userService;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        DataSource ds = (DataSource) getServletContext().getAttribute(DataSourceListener.DATASOURCE_ATTR);
        userService = new UserService(new UserDAOImpl(ds));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<String, String> body = gson.fromJson(req.getReader(), Map.class);

        try {
            User.Role role = User.Role.valueOf(body.getOrDefault("role", "").toUpperCase());
            User created = userService.register(
                body.get("name"), body.get("email"), body.get("password"), role
            );
            Map<String, Object> data = new HashMap<>();
            data.put("id", created.getId());
            data.put("name", created.getName());
            data.put("email", created.getEmail());
            data.put("role", created.getRole());

            writeSuccess(resp, HttpServletResponse.SC_CREATED, data);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        } catch (IllegalStateException e) {
            writeError(resp, HttpServletResponse.SC_CONFLICT, "CONFLICT", e.getMessage());
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
