package com.meenakshi.meenumart.controller;

import com.google.gson.Gson;
import com.meenakshi.meenumart.dao.UserDAOImpl;
import com.meenakshi.meenumart.listener.DataSourceListener;
import com.meenakshi.meenumart.model.User;
import com.meenakshi.meenumart.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/v1/auth/login")
public class LoginServlet extends HttpServlet {

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

        Optional<User> userOpt = userService.authenticate(body.get("email"), body.get("password"));

        if (userOpt.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "INVALID_CREDENTIALS");
            error.put("message", "Email or password is incorrect");
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("success", false);
            envelope.put("data", null);
            envelope.put("error", error);
            resp.getWriter().write(gson.toJson(envelope));
            return;
        }

        User user = userOpt.get();

        // Invalidate any existing session, then start a fresh one — regenerates the session ID on login
        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole().name());
        session.setMaxInactiveInterval(30 * 60); // 30 minute timeout

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());

        Map<String, Object> envelope = new HashMap<>();
        envelope.put("success", true);
        envelope.put("data", data);
        envelope.put("error", null);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(envelope));
    }
}
