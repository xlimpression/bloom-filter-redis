package com.singhand.bloomFilter.repository;

import com.singhand.bloomFilter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username );
}
