package com.meenakshi.meenumart.service;

import com.meenakshi.meenumart.dao.UserDAO;
import com.meenakshi.meenumart.model.User;
import com.meenakshi.meenumart.util.PasswordUtil;
import com.meenakshi.meenumart.util.ValidationUtil;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(String name, String email, String password, User.Role role) {
        if (!ValidationUtil.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name is required");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (role == User.Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot self-register");
        }
        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setRole(role);
        return userDAO.create(user);
    }

    public Optional<User> authenticate(String email, String password) {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isNonEmpty(password)) {
            return Optional.empty();
        }
        Optional<User> userOpt = userDAO.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent() && PasswordUtil.matches(password, userOpt.get().getPasswordHash())) {
            return userOpt;
        }
        return Optional.empty();
    }
}
