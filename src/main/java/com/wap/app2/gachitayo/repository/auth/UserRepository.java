package com.wap.app2.gachitayo.repository.auth;

import com.wap.app2.gachitayo.domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
