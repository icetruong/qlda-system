package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
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
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.exception.ApiException;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.NhomQuyenRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final NguoiDungRepository nguoiDungRepository;
    private final DonViRepository donViRepository;
    private final NhomQuyenRepository nhomQuyenRepository;
    private final AuditLogService auditLogService;

    public UserService(
            NguoiDungRepository nguoiDungRepository,
            DonViRepository donViRepository,
            NhomQuyenRepository nhomQuyenRepository,
            AuditLogService auditLogService
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.donViRepository = donViRepository;
        this.nhomQuyenRepository = nhomQuyenRepository;
        this.auditLogService = auditLogService;
    }

    public PageData<UserSummaryResponse> getUsers(Pageable pageable, String keyword, Integer donViId, Integer trangThai) {
        Specification<NguoiDung> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String normalized = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("userName")), normalized),
                        cb.like(cb.lower(root.get("hoTen")), normalized),
                        cb.like(cb.lower(root.get("email")), normalized)
                ));
            }
            if (donViId != null) {
                predicates.add(cb.equal(root.get("donVi").get("id"), donViId));
            }
            if (trangThai != null) {
                predicates.add(cb.equal(root.get("trangThai"), trangThai));
            } else {
                predicates.add(cb.notEqual(root.get("trangThai"), -1));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<NguoiDung> page = nguoiDungRepository.findAll(specification, pageable);
        List<UserSummaryResponse> content = page.getContent().stream().map(this::toUserSummary).toList();
        return new PageData<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public UserDetailResponse getUserById(Long id) {
        return toUserDetail(findActiveUserById(id));
    }

    public IdResponse createUser(UserCreateRequest request) {
        validateUniqueUser(request.username(), request.email(), null);
        DonVi donVi = findDonViById(request.donViId());
        NhomQuyen nhomQuyen = findRoleById(request.nhomQuyenId());

        NguoiDung user = new NguoiDung();
        user.setUserName(request.username().trim());
        user.setHoTen(request.hoTen().trim());
        user.setEmail(request.email().trim());
        user.setDienThoai(request.dienThoai());
        user.setDonVi(donVi);
        user.setChucVu(request.chucVu());
        user.setNhomQuyen(nhomQuyen);
        user.setAzureAdId(request.azureAdId());
        user.setTrangThai(1);
        user.setNgayTao(LocalDateTime.now());
        user.setNgayCapNhat(LocalDateTime.now());

        NguoiDung saved = nguoiDungRepository.save(user);
        auditLogService.log(saved.getId(), saved.getHoTen(), "CREATE_USER", "NguoiDung",
                saved.getId(), "Create user", "127.0.0.1", 1);
        return new IdResponse(saved.getId());
    }

    public IdResponse updateUser(Long id, UserUpdateRequest request) {
        NguoiDung user = findActiveUserById(id);
        validateUniqueUser(user.getUserName(), request.email(), id);
        user.setHoTen(request.hoTen().trim());
        user.setEmail(request.email().trim());
        user.setDienThoai(request.dienThoai());
        user.setDonVi(findDonViById(request.donViId()));
        user.setChucVu(request.chucVu());
        user.setNhomQuyen(findRoleById(request.nhomQuyenId()));
        user.setTrangThai(request.trangThai());
        user.setNgayCapNhat(LocalDateTime.now());
        nguoiDungRepository.save(user);
        auditLogService.log(user.getId(), user.getHoTen(), "UPDATE_USER", "NguoiDung",
                user.getId(), "Update user", "127.0.0.1", 1);
        return new IdResponse(user.getId());
    }

    public UserStatusResponse updateStatus(Long id, UserStatusUpdateRequest request) {
        NguoiDung user = findActiveUserById(id);
        user.setTrangThai(request.trangThai());
        user.setNgayCapNhat(LocalDateTime.now());
        nguoiDungRepository.save(user);
        auditLogService.log(user.getId(), user.getHoTen(), "UPDATE_USER_STATUS", "NguoiDung",
                user.getId(), "Update status", "127.0.0.1", 1);
        return new UserStatusResponse(user.getId(), user.getTrangThai());
    }

    public UserRoleAssignResponse assignRole(Long id, UserRoleAssignRequest request) {
        NguoiDung user = findActiveUserById(id);
        NhomQuyen role = findRoleById(request.nhomQuyenId());
        user.setNhomQuyen(role);
        user.setNgayCapNhat(LocalDateTime.now());
        nguoiDungRepository.save(user);
        auditLogService.log(user.getId(), user.getHoTen(), "ASSIGN_ROLE", "NguoiDung",
                user.getId(), "Assign role", "127.0.0.1", 1);
        return new UserRoleAssignResponse(user.getId(), role.getId());
    }

    public IdResponse deleteUser(Long id) {
        NguoiDung user = findActiveUserById(id);
        user.setTrangThai(-1);
        user.setNgayCapNhat(LocalDateTime.now());
        nguoiDungRepository.save(user);
        auditLogService.log(user.getId(), user.getHoTen(), "DELETE_USER", "NguoiDung",
                user.getId(), "Soft delete user", "127.0.0.1", 1);
        return new IdResponse(user.getId());
    }

    public SyncAzureUsersResponse syncAzureUsers(SyncAzureUsersRequest request) {
        int totalSynced = 0;
        for (String azureAdId : request.azureAdIds()) {
            if (!StringUtils.hasText(azureAdId)) {
                continue;
            }
            totalSynced += nguoiDungRepository.findByAzureAdId(azureAdId.trim())
                    .map(user -> {
                        user.setNgayCapNhat(LocalDateTime.now());
                        nguoiDungRepository.save(user);
                        return 1;
                    })
                    .orElse(0);
        }
        auditLogService.log(0L, "SYSTEM", "SYNC_AZURE_USERS", "NguoiDung",
                0L, "Sync Azure users", "127.0.0.1", 1);
        return new SyncAzureUsersResponse(totalSynced);
    }

    public NguoiDung findActiveUserByUsername(String username) {
        return nguoiDungRepository.findByUserName(username)
                .filter(user -> user.getTrangThai() != null && user.getTrangThai() == 1)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, "User not found"));
    }

    public NguoiDung findActiveUserById(Long id) {
        return nguoiDungRepository.findById(id)
                .filter(user -> user.getTrangThai() != null && user.getTrangThai() != -1)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, "User not found"));
    }

    private void validateUniqueUser(String username, String email, Long excludeId) {
        if (excludeId == null) {
            if (nguoiDungRepository.existsByUserName(username)) {
                throw new ApiException(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_USERNAME, "Username already exists");
            }
            if (nguoiDungRepository.existsByEmail(email)) {
                throw new ApiException(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_EMAIL, "Email already exists");
            }
            return;
        }
        if (nguoiDungRepository.existsByUserNameAndIdNot(username, excludeId)) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_USERNAME, "Username already exists");
        }
        if (nguoiDungRepository.existsByEmailAndIdNot(email, excludeId)) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_EMAIL, "Email already exists");
        }
    }

    private DonVi findDonViById(Integer donViId) {
        return donViRepository.findById(donViId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.INVALID_REQUEST,
                        "DonVi not found: " + donViId
                ));
    }

    private NhomQuyen findRoleById(Integer nhomQuyenId) {
        return nhomQuyenRepository.findById(nhomQuyenId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.ROLE_NOT_FOUND,
                        "Role not found"
                ));
    }

    private UserSummaryResponse toUserSummary(NguoiDung user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUserName(),
                user.getHoTen(),
                user.getEmail(),
                user.getDienThoai(),
                user.getDonVi() == null ? null : user.getDonVi().getId(),
                user.getDonVi() == null ? null : user.getDonVi().getTenDonVi(),
                user.getChucVu(),
                user.getNhomQuyen() == null ? null : user.getNhomQuyen().getId(),
                user.getNhomQuyen() == null ? null : user.getNhomQuyen().getTenNhomQuyen(),
                user.getTrangThai()
        );
    }

    private UserDetailResponse toUserDetail(NguoiDung user) {
        return new UserDetailResponse(
                user.getId(),
                user.getUserName(),
                user.getHoTen(),
                user.getEmail(),
                user.getDienThoai(),
                user.getDonVi() == null ? null : user.getDonVi().getId(),
                user.getChucVu(),
                user.getNhomQuyen() == null ? null : user.getNhomQuyen().getId(),
                user.getAzureAdId(),
                user.getTrangThai()
        );
    }
}
