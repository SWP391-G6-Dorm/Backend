package com.homestay.controller;

import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.EmployeeRoomResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.User;
import com.homestay.service.EmployeeRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * EmployeeController — endpoints scoped to authenticated Employee users.
 * Base path: /api/employee
 */
@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {

    private final EmployeeRoomService employeeRoomService;

    public EmployeeController(EmployeeRoomService employeeRoomService) {
        this.employeeRoomService = employeeRoomService;
    }

    /**
     * SCR-65 — Property Room List (read-only reference for employees).
     * Returns rooms belonging to the property the authenticated employee is assigned to.
     *
     * GET /api/employee/rooms?status=AVAILABLE&page=0&size=20
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeRoomResponse>>> getRooms(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<EmployeeRoomResponse> result =
                employeeRoomService.getRoomsForEmployee(currentUser, status, page, size);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
