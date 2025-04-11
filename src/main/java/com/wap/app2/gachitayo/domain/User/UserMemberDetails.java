package com.wap.app2.gachitayo.domain.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserMemberDetails implements UserDetails {
    private final User user;

    public UserMemberDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    /**
     * 유저이름 대신 이메일 리턴함
     * @return return user email instead of username
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
