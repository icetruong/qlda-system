package com.qlda.workflowservice.client;

import com.qlda.workflowservice.client.dto.AuthPermissionCheckResponse;
import com.qlda.workflowservice.client.dto.AuthUnitDto;
import com.qlda.workflowservice.client.dto.AuthUserDto;
import com.qlda.workflowservice.client.dto.AuthUserRolesDto;

import java.util.List;

public interface AuthServiceClient {
    AuthUserDto getUserById(Long id);

    void validateUsers(List<Long> userIds);

    AuthUnitDto getUnitById(Integer id);

    void validateUnits(List<Integer> unitIds);

    AuthUserRolesDto getUserRoles(Long id);

    AuthPermissionCheckResponse checkPermission(Long userId, String maChucNang, String permission);
}
