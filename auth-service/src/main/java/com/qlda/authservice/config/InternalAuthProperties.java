package com.qlda.authservice.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.auth")
public class InternalAuthProperties {

    private String token;
    private List<String> allowedServices = new ArrayList<>();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getAllowedServices() {
        return allowedServices;
    }

    public void setAllowedServices(List<String> allowedServices) {
        this.allowedServices = allowedServices;
    }
}
