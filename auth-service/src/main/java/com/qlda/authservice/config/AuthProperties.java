package com.qlda.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private final Jwt jwt = new Jwt();
    private final DevPassword devPassword = new DevPassword();
    private final Backup backup = new Backup();
    private final Azure azure = new Azure();
    private final Office365 office365 = new Office365();
    private final SecurityPolicy securityPolicy = new SecurityPolicy();
    private final SystemConfig systemConfig = new SystemConfig();

    public Jwt getJwt() {
        return jwt;
    }

    public DevPassword getDevPassword() {
        return devPassword;
    }

    public Backup getBackup() {
        return backup;
    }

    public Azure getAzure() {
        return azure;
    }

    public Office365 getOffice365() {
        return office365;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public static class Jwt {
        private String issuer;
        private String secret;
        private String privateKey;
        private String publicKey;
        private long accessTokenSeconds = 3600;
        private long refreshTokenSeconds = 604800;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public long getAccessTokenSeconds() {
            return accessTokenSeconds;
        }

        public void setAccessTokenSeconds(long accessTokenSeconds) {
            this.accessTokenSeconds = accessTokenSeconds;
        }

        public long getRefreshTokenSeconds() {
            return refreshTokenSeconds;
        }

        public void setRefreshTokenSeconds(long refreshTokenSeconds) {
            this.refreshTokenSeconds = refreshTokenSeconds;
        }
    }

    public static class DevPassword {
        private boolean enabled = true;
        private String value;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Backup {
        private String directory = "./backups";

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }

    public static class Azure {
        private String tenantId;
        private String clientId;
        private String clientSecret;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    public static class Office365 {
        private String tenantId;
        private String clientId;
        private String clientSecret;
        private String sharePoint;
        private String teams;
        private String outlook;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getSharePoint() {
            return sharePoint;
        }

        public void setSharePoint(String sharePoint) {
            this.sharePoint = sharePoint;
        }

        public String getTeams() {
            return teams;
        }

        public void setTeams(String teams) {
            this.teams = teams;
        }

        public String getOutlook() {
            return outlook;
        }

        public void setOutlook(String outlook) {
            this.outlook = outlook;
        }
    }

    public static class SecurityPolicy {
        private int sessionTimeoutMinutes = 30;
        private int maxLoginAttempts = 5;
        private boolean requireAuthentication = true;
        private boolean enableCors = true;
        private boolean enableIpWhitelist = false;

        public int getSessionTimeoutMinutes() {
            return sessionTimeoutMinutes;
        }

        public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
            this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        }

        public int getMaxLoginAttempts() {
            return maxLoginAttempts;
        }

        public void setMaxLoginAttempts(int maxLoginAttempts) {
            this.maxLoginAttempts = maxLoginAttempts;
        }

        public boolean isRequireAuthentication() {
            return requireAuthentication;
        }

        public void setRequireAuthentication(boolean requireAuthentication) {
            this.requireAuthentication = requireAuthentication;
        }

        public boolean isEnableCors() {
            return enableCors;
        }

        public void setEnableCors(boolean enableCors) {
            this.enableCors = enableCors;
        }

        public boolean isEnableIpWhitelist() {
            return enableIpWhitelist;
        }

        public void setEnableIpWhitelist(boolean enableIpWhitelist) {
            this.enableIpWhitelist = enableIpWhitelist;
        }
    }

    public static class SystemConfig {
        private String appName;
        private String environment;
        private String maxUploadFileSize;
        private boolean backupEnabled = true;

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getMaxUploadFileSize() {
            return maxUploadFileSize;
        }

        public void setMaxUploadFileSize(String maxUploadFileSize) {
            this.maxUploadFileSize = maxUploadFileSize;
        }

        public boolean isBackupEnabled() {
            return backupEnabled;
        }

        public void setBackupEnabled(boolean backupEnabled) {
            this.backupEnabled = backupEnabled;
        }
    }
}
