package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.security.PermissionCheckRequest;
import com.qlda.authservice.dto.security.PermissionCheckResponse;
import com.qlda.authservice.dto.security.SecurityPolicyResponse;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.PhanQuyen;
import com.qlda.authservice.exception.ApiException;
import com.qlda.authservice.repository.PhanQuyenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SecurityPolicyService {

    private final AuthProperties authProperties;
    private final UserService userService;
    private final PhanQuyenRepository phanQuyenRepository;

    public SecurityPolicyService(
            AuthProperties authProperties,
            UserService userService,
            PhanQuyenRepository phanQuyenRepository
    ) {
        this.authProperties = authProperties;
        this.userService = userService;
        this.phanQuyenRepository = phanQuyenRepository;
    }

    public SecurityPolicyResponse getSecurityPolicies() {
        AuthProperties.SecurityPolicy policy = authProperties.getSecurityPolicy();
        return new SecurityPolicyResponse(
                policy.getSessionTimeoutMinutes(),
                policy.getMaxLoginAttempts(),
                policy.isRequireAuthentication(),
                policy.isEnableCors(),
                policy.isEnableIpWhitelist()
        );
    }

    public PermissionCheckResponse checkPermission(PermissionCheckRequest request) {
        NguoiDung user = userService.findActiveUserById(request.userId());
        if (user.getNhomQuyen() == null) {
            return new PermissionCheckResponse(false);
        }

        PhanQuyen permission = phanQuyenRepository
                .findByNhomQuyenAndMaChucNang(user.getNhomQuyen().getId(), request.maChucNang())
                .orElse(null);
        if (permission == null) {
            return new PermissionCheckResponse(false);
        }
        return new PermissionCheckResponse(hasPermission(permission, request.permission()));
    }

    private boolean hasPermission(PhanQuyen permission, String action) {
        if (!StringUtils.hasText(action)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "permission is required");
        }
        return switch (action.trim().toUpperCase()) {
            case "VIEW" -> Boolean.TRUE.equals(permission.getIsView());
            case "CREATE" -> Boolean.TRUE.equals(permission.getIsCreate());
            case "EDIT" -> Boolean.TRUE.equals(permission.getIsEdit());
            case "DELETE" -> Boolean.TRUE.equals(permission.getIsDelete());
            case "APPROVE" -> Boolean.TRUE.equals(permission.getIsApprove());
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.PERMISSION_NOT_FOUND, "Permission type not found");
        };
    }
}
