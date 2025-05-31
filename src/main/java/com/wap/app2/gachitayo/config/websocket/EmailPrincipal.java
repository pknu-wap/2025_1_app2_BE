package com.wap.app2.gachitayo.config.websocket;

import java.security.Principal;

public class EmailPrincipal implements Principal {
    private final String email;
    public EmailPrincipal(String email) {
        this.email = email;
    }
    @Override
    public String getName() {
        return email;
    }
}
