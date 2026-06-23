package com.homestay.service;

import com.homestay.dto.response.BookingSummaryResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.Booking;
import com.homestay.entity.User;
import com.homestay.exception.ForbiddenException;
import com.homestay.repository.BookingRepository;
import com.homestay.repository.PaymentRepository;
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
    private final NotificationService notificationService;
    private final com.homestay.repository.RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final com.homestay.repository.ReviewRepository reviewRepository;

    public BookingService(BookingRepository bookingRepository,
                          NotificationService notificationService,
                          com.homestay.repository.RoomRepository roomRepository,
                          PaymentRepository paymentRepository,
                          com.homestay.repository.ReviewRepository reviewRepository) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
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
            result = bookingRepository.findByCustomerIdAndStatus(
                    currentUser.getId(), bookingStatus, pageable);
        } else {
            result = bookingRepository.findByCustomerId(currentUser.getId(), pageable);
        }

        return new PageResponse<>(
                result.getContent().stream().map(booking -> {
                    boolean isReviewed = reviewRepository.existsByBooking_Id(booking.getId());
                    return BookingSummaryResponse.fromEntity(booking, isReviewed);
                }).toList(),
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
                result.getContent().stream().map(booking -> {
                    boolean isReviewed = reviewRepository.existsByBooking_Id(booking.getId());
                    return BookingSummaryResponse.fromEntity(booking, isReviewed);
                }).toList(),
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

        boolean isReviewed = reviewRepository.existsByBooking_Id(booking.getId());
        BookingDetailResponse response = BookingDetailResponse.fromEntity(booking, isReviewed);
        response.setPayments(
                paymentRepository.findByBookingIdOrderByCreatedAtDesc(id).stream()
                        .map(BookingDetailResponse.PaymentInfo::fromEntity)
                        .toList()
        );
        return response;
    }

    @org.springframework.transaction.annotation.Transactional
    public void cancelBooking(UUID id, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new com.homestay.exception.ResourceNotFoundException("Booking không tồn tại"));

        boolean isManager = currentUser.getRole() == User.Role.MANAGER;
        if (!isManager && !booking.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Không có quyền hủy booking này");
        }

        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new IllegalArgumentException("Booking đã bị hủy trước đó");
        }
        if (booking.getStatus() == Booking.Status.CHECKED_IN || booking.getStatus() == Booking.Status.CHECKED_OUT) {
            throw new IllegalArgumentException("Không thể hủy booking đang check-in hoặc đã check-out");
        }

        booking.setStatus(Booking.Status.CANCELLED);
        // Giải phóng phòng về AVAILABLE nếu đang ở trạng thái bị giữ
        Room room = booking.getRoom();
        if (room.getStatus() != Room.Status.OCCUPIED && room.getStatus() != Room.Status.MAINTENANCE) {
            room.setStatus(Room.Status.AVAILABLE);
        }
        bookingRepository.save(booking);

        notificationService.sendNotification(
                booking.getCustomer().getId(),
                com.homestay.entity.Notification.Type.BOOKING_CANCELLED,
                "Booking Cancelled",
                "Your booking #" + booking.getId().toString().substring(0, 8).toUpperCase() + " has been cancelled.",
                booking.getId(), "Booking"
        );
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

        // Gửi thông báo cho Customer
        String roomName = booking.getRoom().getRoomNumber();
        notificationService.sendNotification(
                booking.getCustomer().getId(),
                com.homestay.entity.Notification.Type.BOOKING_CONFIRMED,
                "Booking Confirmed",
                "Your booking for " + roomName + " has been confirmed. Check-in on " + booking.getCheckInDate() + ".",
                booking.getId(), "Booking"
        );
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

        // Gửi thông báo cho Customer
        String roomName = booking.getRoom().getRoomNumber();
        notificationService.sendNotification(
                booking.getCustomer().getId(),
                com.homestay.entity.Notification.Type.BOOKING_CONFIRMED,
                "Check-out Complete",
                "Thank you for your stay at " + roomName + ". We hope to see you again!",
                booking.getId(), "Booking"
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public BookingDetailResponse createBooking(com.homestay.dto.request.CreateBookingRequest request, User currentUser) {
        if (currentUser.getRole() != User.Role.CUSTOMER) {
            throw new ForbiddenException("Chỉ khách hàng mới có thể đặt phòng");
        }

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("Ngày check-out phải sau ngày check-in");
        }

        if (request.getCheckInDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Ngày check-in không được trong quá khứ");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new com.homestay.exception.ResourceNotFoundException("Phòng không tồn tại"));

        if (room.getStatus() != Room.Status.AVAILABLE) {
            throw new IllegalArgumentException("Phòng hiện không trống để đặt");
        }

        if (request.getGuestCount() > room.getCapacity()) {
            throw new IllegalArgumentException("Số người vượt quá sức chứa của phòng");
        }

        boolean isOverlap = roomRepository.existsOverlapBooking(room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (isOverlap) {
            throw new IllegalArgumentException("Phòng đã có người đặt trong khoảng thời gian này");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights <= 0) nights = 1;

        java.math.BigDecimal totalAmount = room.getPricePerNight().multiply(java.math.BigDecimal.valueOf(nights));
        java.math.BigDecimal depositAmount = totalAmount.multiply(java.math.BigDecimal.valueOf(0.40));
        java.math.BigDecimal remainingAmount = totalAmount.subtract(depositAmount);

        Booking booking = new Booking();
        booking.setCustomer(currentUser);
        booking.setRoom(room);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuestCount(request.getGuestCount());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setTotalAmount(totalAmount);
        booking.setDepositAmount(depositAmount);
        booking.setRemainingAmount(remainingAmount);
        booking.setStatus(Booking.Status.PENDING_DEPOSIT);

        booking = bookingRepository.save(booking);

        // Giữ phòng ở trạng thái chờ cọc để tránh double-booking
        room.setStatus(Room.Status.PENDING_DEPOSIT);

        return BookingDetailResponse.fromEntity(booking);
    }
}
