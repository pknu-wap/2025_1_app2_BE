package com.wap.app2.gachitayo.domain.Member;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MemberDetails implements UserDetails {
    private final String email;

    public MemberDetails(String email) {
        this.email = email;
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
        return email;
    }
}
