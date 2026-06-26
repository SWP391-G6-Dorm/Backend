package com.homestay.controller;

import com.homestay.config.VNPayConfig;
import com.homestay.dto.response.ApiResponse;
import com.homestay.entity.Booking;
import com.homestay.entity.Payment;
import com.homestay.entity.User;
import com.homestay.exception.ForbiddenException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.BookingRepository;
import com.homestay.repository.PaymentRepository;
import com.homestay.service.ContractService;
import com.homestay.service.VNPayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class CustomerPaymentController {

    private final VNPayService vnPayService;
    private final VNPayConfig vnPayConfig;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ContractService contractService;

    public CustomerPaymentController(VNPayService vnPayService, VNPayConfig vnPayConfig,
                                     BookingRepository bookingRepository, PaymentRepository paymentRepository,
                                     ContractService contractService) {
        this.vnPayService = vnPayService;
        this.vnPayConfig = vnPayConfig;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.contractService = contractService;
    }

    @PostMapping("/vnpay/create-url")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPaymentUrl(
            @RequestParam UUID bookingId,
            @RequestParam String type,
            @AuthenticationPrincipal User currentUser) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại"));

        if (!booking.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền thanh toán cho booking này");
        }

        if (type.equals("DEPOSIT") && booking.getStatus() != Booking.Status.PENDING_DEPOSIT) {
            throw new IllegalArgumentException("Booking này không ở trạng thái chờ đặt cọc");
        }

        long amount = 0;
        if (type.equals("DEPOSIT")) {
            amount = booking.getTotalAmount().multiply(new java.math.BigDecimal("0.4")).longValue();
        } else if (type.equals("REMAINING_BALANCE")) {
            amount = booking.getTotalAmount().multiply(new java.math.BigDecimal("0.6")).longValue();
        }

        // Tạo Payment record PENDING
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setCustomer(currentUser);
        payment.setType(Payment.Type.valueOf(type));
        payment.setMethod(Payment.Method.E_WALLET);
        payment.setAmount(java.math.BigDecimal.valueOf(amount));
        payment.setStatus(Payment.Status.PENDING);
        paymentRepository.save(payment);

        String orderInfo = "Thanh toan " + type + " cho Booking " + booking.getId();
        String paymentUrl = vnPayService.createOrder(amount, orderInfo, vnPayConfig.getVnp_ReturnUrl(), payment.getId().toString());

        Map<String, String> result = new HashMap<>();
        result.put("paymentUrl", paymentUrl);

        return ResponseEntity.ok(ApiResponse.ok("Tạo URL thanh toán thành công", result));
    }

    @GetMapping("/vnpay/return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0) && fieldName.startsWith("vnp_")) {
                fields.put(fieldName, fieldValue);
            }
        }

        String paymentIdStr = request.getParameter("paymentId");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String frontendRedirectUrl = "http://localhost:3000/customer/payments/vnpay-result";

        if (paymentIdStr == null) {
            response.sendRedirect(frontendRedirectUrl + "?status=failed&message=Missing_Payment_ID");
            return;
        }

        UUID paymentId;
        try {
            paymentId = UUID.fromString(paymentIdStr);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(frontendRedirectUrl + "?status=failed&message=Invalid_Payment_ID");
            return;
        }

        if (vnPayService.verifySignature(fields)) {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) {
                response.sendRedirect(frontendRedirectUrl + "?status=failed&message=Payment_Not_Found");
                return;
            }

            if ("00".equals(vnp_ResponseCode)) {
                if (payment.getStatus() == Payment.Status.PENDING) {
                    payment.setStatus(Payment.Status.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    
                    Booking booking = payment.getBooking();
                    if (payment.getType() == Payment.Type.DEPOSIT && booking.getStatus() == Booking.Status.PENDING_DEPOSIT) {
                        booking.setStatus(Booking.Status.CONFIRMED);
                        bookingRepository.save(booking);
                        
                        try {
                            // User verification uses current context which is null here for return URL.
                            // contractService usually needs a user. Let's pass the customer user.
                            contractService.autoGenerateAndSendContract(booking.getId(), payment.getCustomer());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    paymentRepository.save(payment);
                }
                response.sendRedirect(frontendRedirectUrl + "?status=success&bookingId=" + payment.getBooking().getId());
            } else {
                payment.setStatus(Payment.Status.FAILED);
                paymentRepository.save(payment);
                response.sendRedirect(frontendRedirectUrl + "?status=failed&message=Payment_Failed");
            }
        } else {
            response.sendRedirect(frontendRedirectUrl + "?status=failed&message=Invalid_Signature");
        }
    }
}
