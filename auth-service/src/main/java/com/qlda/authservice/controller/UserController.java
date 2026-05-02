package com.qlda.authservice.controller;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.common.IdResponse;
import com.qlda.authservice.dto.common.PageData;
import com.qlda.authservice.dto.user.SyncAzureUsersRequest;
import com.qlda.authservice.dto.user.SyncAzureUsersResponse;
import com.qlda.authservice.dto.user.UserCreateRequest;
import com.qlda.authservice.dto.user.UserDetailResponse;
import com.qlda.authservice.dto.user.UserRoleAssignRequest;
import com.qlda.authservice.dto.user.UserRoleAssignResponse;
import com.qlda.authservice.dto.user.UserStatusResponse;
import com.qlda.authservice.dto.user.UserStatusUpdateRequest;
import com.qlda.authservice.dto.user.UserSummaryResponse;
import com.qlda.authservice.dto.user.UserUpdateRequest;
import com.qlda.authservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageData<UserSummaryResponse>> getUsers(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be >= 1") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer donViId,
            @RequestParam(required = false) Integer trangThai
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ApiResponse.success("Get users successfully", userService.getUsers(pageRequest, keyword, donViId, trangThai));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success("Get user detail successfully", userService.getUserById(id));
    }

    @PostMapping
    public ApiResponse<IdResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success("Create user successfully", userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<IdResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success("Update user successfully", userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserStatusResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        return ApiResponse.success("Update user status successfully", userService.updateStatus(id, request));
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<UserRoleAssignResponse> assignRole(@PathVariable Long id, @Valid @RequestBody UserRoleAssignRequest request) {
        return ApiResponse.success("Assign role successfully", userService.assignRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<IdResponse> deleteUser(@PathVariable Long id) {
        return ApiResponse.success("Delete user successfully", userService.deleteUser(id));
    }

    @PostMapping("/sync-azure")
    public ApiResponse<SyncAzureUsersResponse> syncAzureUsers(@Valid @RequestBody SyncAzureUsersRequest request) {
        return ApiResponse.success("Sync Azure users successfully", userService.syncAzureUsers(request));
    }
}
