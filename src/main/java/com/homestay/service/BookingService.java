package com.homestay.service;

import com.homestay.dto.response.BookingSummaryResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.Booking;
import com.homestay.entity.User;
import com.homestay.exception.ForbiddenException;
import com.homestay.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public PageResponse<BookingSummaryResponse> getMyBookings(
            User currentUser,
            int page,
            int size,
            String status,
            String sort
    ) {
        if (currentUser.getRole() != User.Role.CUSTOMER) {
            throw new ForbiddenException("Chỉ khách hàng mới có danh sách đặt phòng");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Booking> result;

        if (status != null && !status.isBlank()) {
            Booking.Status bookingStatus = Booking.Status.valueOf(status.trim().toUpperCase());
            result = bookingRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                    currentUser.getId(), bookingStatus, pageable);
        } else {
            result = bookingRepository.findByCustomerIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        }

        return new PageResponse<>(
                result.getContent().stream().map(BookingSummaryResponse::fromEntity).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public static Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        List<String> allowed = Arrays.asList("createdAt", "checkInDate", "totalAmount", "status");
        if (!allowed.contains(field)) {
            field = "createdAt";
        }
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}
