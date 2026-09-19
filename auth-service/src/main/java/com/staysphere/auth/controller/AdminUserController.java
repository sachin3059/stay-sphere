package com.staysphere.auth.controller;

import com.staysphere.auth.dto.AdminUserResponse;
import com.staysphere.auth.dto.UpdateUserRoleRequest;
import com.staysphere.auth.service.AdminUserService;
import com.staysphere.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched",
                        adminUserService.listUsers()));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateRole(
            @PathVariable String userId,
            @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("User role updated",
                        adminUserService.updateRole(userId, request.getRole())));
    }
}
