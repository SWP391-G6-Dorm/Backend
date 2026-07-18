package com.homestay.service;

import com.homestay.config.VNPayConfig;
import com.homestay.dto.request.PaymentVerificationRequest;
import com.homestay.entity.Booking;
import com.homestay.entity.Payment;
import com.homestay.entity.Property;
import com.homestay.entity.Room;
import com.homestay.entity.User;
import com.homestay.exception.ForbiddenException;
import com.homestay.repository.BookingRepository;
import com.homestay.repository.ManagerPropertyAssignmentRepository;
import com.homestay.repository.PaymentRepository;
import com.homestay.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceVerifyTest {

    @Mock PaymentRepository paymentRepository;
    @Mock BookingRepository bookingRepository;
    @Mock ContractService contractService;
    @Mock VNPayService vnPayService;
    @Mock VNPayConfig vnPayConfig;
    @Mock ReportPropertyScopeValidator scopeValidator;
    @Mock ManagerPropertyAssignmentRepository assignmentRepository;

    private PaymentService service;
    private User manager;
    private User customer;
    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setUp() {
        service = new PaymentService(
                paymentRepository,
                bookingRepository,
                contractService,
                vnPayService,
                vnPayConfig,
                scopeValidator,
                assignmentRepository);
        manager = TestFixtures.user(User.Role.MANAGER);
        customer = TestFixtures.user(User.Role.CUSTOMER);
        Property property = TestFixtures.property();
        Room room = TestFixtures.room(property);
        booking = TestFixtures.booking(customer, room, Booking.Status.PENDING_DEPOSIT);
        payment = TestFixtures.payment(booking, Payment.Type.DEPOSIT, Payment.Status.PENDING);
    }

    @Test
    void verifyPayment_customerForbidden() {
        PaymentVerificationRequest req = new PaymentVerificationRequest();
        req.setStatus(Payment.Status.PAID);
        req.setNote("ok");

        assertThrows(ForbiddenException.class,
                () -> service.verifyPayment(payment.getId(), req, customer));
    }

    @Test
    void verifyPayment_nonPendingRejected() {
        payment.setStatus(Payment.Status.PAID);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        PaymentVerificationRequest req = new PaymentVerificationRequest();
        req.setStatus(Payment.Status.FAILED);
        req.setNote("late reject");

        assertThrows(IllegalArgumentException.class,
                () -> service.verifyPayment(payment.getId(), req, manager));
    }

    @Test
    void verifyPayment_paidDeposit_confirmsBooking() {
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentVerificationRequest req = new PaymentVerificationRequest();
        req.setStatus(Payment.Status.PAID);
        req.setNote("Bank transfer verified with receipt");

        var resp = service.verifyPayment(payment.getId(), req, manager);

        assertEquals(Payment.Status.PAID, resp.getStatus());
        assertEquals(Booking.Status.CONFIRMED, booking.getStatus());
        assertEquals("Bank transfer verified with receipt", payment.getVerificationNote());
        verify(paymentRepository).save(payment);
    }
}
