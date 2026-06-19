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
import com.homestay.dto.response.BookingDetailResponse;
import com.homestay.entity.Room;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<BookingSummaryResponse> getAllBookings(
            int page,
            int size,
            String status,
            String search,
            String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        Booking.Status bookingStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL"))
                ? Booking.Status.valueOf(status.trim().toUpperCase()) : null;

        Page<Booking> result = bookingRepository.findAllWithFilters(bookingStatus, search, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(BookingSummaryResponse::fromEntity).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetail(UUID id, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new com.homestay.exception.ResourceNotFoundException("Booking không tồn tại"));

        boolean isManager = currentUser.getRole() == User.Role.MANAGER;

        if (!isManager && !booking.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Không có quyền xem chi tiết đặt phòng này");
        }

        return BookingDetailResponse.fromEntity(booking);
    }

    @org.springframework.transaction.annotation.Transactional
    public void markAsCheckedIn(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new com.homestay.exception.ResourceNotFoundException("Booking không tồn tại"));

        if (booking.getStatus() != Booking.Status.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể check-in khi booking ở trạng thái CONFIRMED");
        }

        booking.setStatus(Booking.Status.CHECKED_IN);
        booking.getRoom().setStatus(Room.Status.OCCUPIED);
        bookingRepository.save(booking);
    }

    @org.springframework.transaction.annotation.Transactional
    public void markAsCheckedOut(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new com.homestay.exception.ResourceNotFoundException("Booking không tồn tại"));

        if (booking.getStatus() != Booking.Status.CHECKED_IN) {
            throw new IllegalArgumentException("Chỉ có thể check-out khi booking ở trạng thái CHECKED_IN");
        }

        booking.setStatus(Booking.Status.CHECKED_OUT);
        booking.getRoom().setStatus(Room.Status.AVAILABLE);
        bookingRepository.save(booking);
    }
}
