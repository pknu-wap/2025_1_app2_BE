package com.wap.app2.gachitayo.repository.auth;

import com.wap.app2.gachitayo.domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
