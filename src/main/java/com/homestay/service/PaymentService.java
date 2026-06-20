package com.homestay.service;

import com.homestay.dto.request.PaymentVerificationRequest;
import com.homestay.dto.response.PageResponse;
import com.homestay.dto.response.PaymentDetailResponse;
import com.homestay.dto.response.PaymentSummaryResponse;
import com.homestay.entity.Booking;
import com.homestay.entity.Payment;
import com.homestay.entity.User;
import com.homestay.exception.ForbiddenException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.BookingRepository;
import com.homestay.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ContractService contractService;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          ContractService contractService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.contractService = contractService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentSummaryResponse> getAllPayments(int page, int size, String status, String search, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Payment.Status paymentStatus = parseStatus(status);
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;

        Page<Payment> result = paymentRepository.findAllWithFilters(paymentStatus, searchParam, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(PaymentSummaryResponse::fromEntity).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetail(UUID id, User currentUser) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment không tồn tại"));

        boolean isManager = currentUser.getRole() == User.Role.MANAGER;
        if (!isManager && !payment.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Không có quyền xem thanh toán này");
        }

        return PaymentDetailResponse.fromEntity(payment);
    }

    @Transactional
    public PaymentDetailResponse verifyPayment(UUID id, PaymentVerificationRequest request, User currentUser) {
        if (currentUser.getRole() != User.Role.MANAGER) {
            throw new ForbiddenException("Chỉ Manager mới có quyền duyệt thanh toán");
        }

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment không tồn tại"));

        if (payment.getStatus() != Payment.Status.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể duyệt thanh toán đang ở trạng thái PENDING");
        }

        payment.setStatus(request.getStatus());
        payment.setVerificationNote(request.getNote());
        payment.setVerifiedBy(currentUser);
        payment.setVerifiedAt(LocalDateTime.now());
        
        if (request.getStatus() == Payment.Status.PAID) {
            payment.setPaidAt(LocalDateTime.now());
            Booking booking = payment.getBooking();
            
            // Theo AGENTS.md: Deposit xong thì Booking -> CONFIRMED và sinh Hợp đồng
            if (payment.getType() == Payment.Type.DEPOSIT) {
                if (booking.getStatus() == Booking.Status.PENDING_DEPOSIT) {
                    booking.setStatus(Booking.Status.CONFIRMED);
                    bookingRepository.save(booking);
                }
                paymentRepository.save(payment);
                
                // Tự động sinh PDF và gửi Email hợp đồng
                try {
                    contractService.autoGenerateAndSendContract(booking.getId(), currentUser);
                } catch (Exception e) {
                    // Log error if needed, but don't rollback payment verification
                    e.printStackTrace();
                }
            } else {
                paymentRepository.save(payment);
            }
        } else {
            paymentRepository.save(payment);
        }

        return PaymentDetailResponse.fromEntity(payment);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        List<String> allowed = Arrays.asList("createdAt", "amount", "status", "type");
        if (!allowed.contains(field)) {
            field = "createdAt";
        }
        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    private Payment.Status parseStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) return null;
        try {
            return Payment.Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
