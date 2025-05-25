package com.wap.app2.gachitayo.domain.auth;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class ImapStoreManager {

    private final String host = "imap.gmail.com";

    private final int port = 993;

    @Value("${spring.sms.email}")
    private String username;

    @Value("${spring.email.password}")
    private String password;

    private final String protocol = "imaps";

    private Store store;

    @PostConstruct
    public void init() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", protocol);
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", String.valueOf(port));
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        store = session.getStore(protocol);
        store.connect(host, username, password);
    }

    public synchronized Store getStore() throws Exception {
        if (!store.isConnected()) {
            store.connect(host, username, password);
        }
        return store;
    }

    @PreDestroy
    public void cleanup() throws Exception {
        if (store != null && store.isConnected()) {
            store.close();
        }
    }
}