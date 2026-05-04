package com.qlda.authservice.service;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.office365.Office365ConfigStatusResponse;
import com.qlda.authservice.dto.security.SecurityPolicyResponse;
import com.qlda.authservice.dto.system.SystemConfigResponse;
import com.qlda.authservice.dto.system.SystemHealthResponse;
import com.qlda.authservice.repository.PhanQuyenRepository;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PhanQuyenRepository phanQuyenRepository;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Test
    void office365StatusShouldReadConfig() {
        AuthProperties props = new AuthProperties();
        props.getOffice365().setTenantId("tenant");
        props.getOffice365().setClientId("client");
        props.getOffice365().setClientSecret("secret");
        props.getOffice365().setSharePoint("sp");
        props.getOffice365().setTeams("teams");
        props.getOffice365().setOutlook("outlook");

        Office365Service office365Service = new Office365Service(props);
        Office365ConfigStatusResponse response = office365Service.getConfigStatus();

        assertTrue(response.tenantIdConfigured());
        assertTrue(response.clientIdConfigured());
        assertTrue(response.clientSecretConfigured());
        assertTrue(response.sharePointConfigured());
        assertTrue(response.teamsConfigured());
        assertTrue(response.outlookConfigured());
    }

    @Test
    void securityPoliciesShouldReadConfig() {
        AuthProperties props = new AuthProperties();
        props.getSecurityPolicy().setSessionTimeoutMinutes(45);
        props.getSecurityPolicy().setMaxLoginAttempts(3);
        props.getSecurityPolicy().setRequireAuthentication(true);
        props.getSecurityPolicy().setEnableCors(false);
        props.getSecurityPolicy().setEnableIpWhitelist(true);

        SecurityPolicyService securityPolicyService = new SecurityPolicyService(props, userService, phanQuyenRepository);
        SecurityPolicyResponse response = securityPolicyService.getSecurityPolicies();

        assertEquals(45, response.sessionTimeoutMinutes());
        assertEquals(3, response.maxLoginAttempts());
        assertTrue(response.requireAuthentication());
        assertEquals(false, response.enableCors());
        assertTrue(response.enableIpWhitelist());
    }

    @Test
    void systemConfigsShouldReadConfig() {
        AuthProperties props = new AuthProperties();
        props.getSystemConfig().setAppName("QLVB System");
        props.getSystemConfig().setEnvironment("dev");
        props.getSystemConfig().setMaxUploadFileSize("20MB");
        props.getSystemConfig().setBackupEnabled(true);
        props.getJwt().setAccessTokenSeconds(7200);

        SystemConfigService systemConfigService = new SystemConfigService(props);
        SystemConfigResponse response = systemConfigService.getSystemConfigs();

        assertEquals("QLVB System", response.appName());
        assertEquals("dev", response.environment());
        assertEquals("20MB", response.maxUploadFileSize());
        assertEquals(7200, response.jwtExpiration());
        assertTrue(response.backupEnabled());
    }

    @Test
    void systemHealthShouldReturnUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        SystemHealthService systemHealthService = new SystemHealthService(dataSource);
        SystemHealthResponse response = systemHealthService.getSystemHealth();

        assertEquals("UP", response.status());
        assertEquals("UP", response.database());
        assertEquals("auth-service", response.service());
    }
}
