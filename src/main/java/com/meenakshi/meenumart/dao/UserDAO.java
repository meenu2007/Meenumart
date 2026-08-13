package com.meenakshi.meenumart.dao;

import com.meenakshi.meenumart.model.User;
import java.util.Optional;

public interface UserDAO {
    User create(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
}
