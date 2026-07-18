package com.homestay.service;

import com.homestay.entity.Booking;
import com.homestay.entity.DamageReport;
import com.homestay.entity.Payment;
import com.homestay.entity.Property;
import com.homestay.entity.Room;
import com.homestay.entity.User;
import com.homestay.exception.BusinessException;
import com.homestay.repository.BookingRepository;
import com.homestay.repository.DamageReportRepository;
import com.homestay.repository.PaymentRepository;
import com.homestay.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DamageFeeSettlementServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock DamageReportRepository damageReportRepository;

    @InjectMocks DamageFeeSettlementService service;

    private Booking booking;
    private DamageReport dr;

    @BeforeEach
    void setUp() {
        User customer = TestFixtures.user(User.Role.CUSTOMER);
        Property property = TestFixtures.property();
        Room room = TestFixtures.room(property);
        booking = TestFixtures.booking(customer, room, Booking.Status.PENDING_INSPECTION);
        dr = TestFixtures.damageReport(
                booking,
                TestFixtures.inspection(booking, property, room),
                new BigDecimal("2000000"),
                false);
    }

    @Test
    void applyApprovedFee_createsSinglePendingDamagePayment() {
        when(paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        service.applyApprovedFee(dr, new BigDecimal("2000000"));

        assertEquals(0, new BigDecimal("2000000").compareTo(booking.getDamageFeeAmount()));
        assertEquals(Booking.Status.PENDING_DAMAGE_PAYMENT, booking.getStatus());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment payment = captor.getValue();
        assertEquals(Payment.Type.DAMAGE_FEE, payment.getType());
        assertEquals(Payment.Status.PENDING, payment.getStatus());
        assertEquals(0, new BigDecimal("2000000").compareTo(payment.getAmount()));
    }

    @Test
    void applyApprovedFee_doesNotDuplicateOpenDamagePayment() {
        Payment existing = TestFixtures.payment(booking, Payment.Type.DAMAGE_FEE, Payment.Status.PENDING);
        when(paymentRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId()))
                .thenReturn(List.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        service.applyApprovedFee(dr, new BigDecimal("2000000"));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void applyApprovedFee_rejectsNonPositiveAmount() {
        assertThrows(BusinessException.class, () -> service.applyApprovedFee(dr, BigDecimal.ZERO));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void markDamageReportPaidForBooking_updatesStatus() {
        DamageReport approved = dr;
        approved.setStatus(DamageReport.Status.APPROVED);
        when(damageReportRepository.findFirstByBooking_IdAndStatus(booking.getId(), DamageReport.Status.APPROVED))
                .thenReturn(Optional.of(approved));

        service.markDamageReportPaidForBooking(booking.getId());

        assertEquals(DamageReport.Status.PAID, approved.getStatus());
        verify(damageReportRepository).save(approved);
    }
}
