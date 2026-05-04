package com.qlda.documentservice.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.auth")
@Getter
@Setter
public class InternalAuthProperties {
    private String serviceName = "document-service";
    private String serviceToken = "change-me-in-dev";
    private List<String> allowedServices = new ArrayList<>();
}
