package com.homestay.controller;

import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.BookingSummaryResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.User;
import com.homestay.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** SCR-11 / SCR-18 — danh sách booking của khách hàng đang đăng nhập */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingSummaryResponse>>> getMyBookings(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PageResponse<BookingSummaryResponse> data =
                bookingService.getMyBookings(currentUser, page, size, status, sort);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
