package com.homestay.service;

import com.homestay.dto.response.ContractDetailResponse;
import com.homestay.dto.response.ContractSummaryResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.Contract;
import com.homestay.entity.Property;
import com.homestay.entity.User;
import com.homestay.exception.ForbiddenException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.ContractRepository;
import com.homestay.repository.ManagerPropertyAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final com.homestay.repository.BookingRepository bookingRepository;
    private final PdfService pdfService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ManagerPropertyAssignmentRepository assignmentRepository;
    private final ReportPropertyScopeValidator scopeValidator;

    public ContractService(ContractRepository contractRepository,
                           com.homestay.repository.BookingRepository bookingRepository,
                           PdfService pdfService, EmailService emailService,
                           NotificationService notificationService,
                           ManagerPropertyAssignmentRepository assignmentRepository,
                           ReportPropertyScopeValidator scopeValidator) {
        this.contractRepository = contractRepository;
        this.bookingRepository = bookingRepository;
        this.pdfService = pdfService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.assignmentRepository = assignmentRepository;
        this.scopeValidator = scopeValidator;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractSummaryResponse> getAllContracts(int page, int size, String status, String search, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Contract.Status contractStatus = parseStatus(status);
        String searchParam = cleanSearch(search);

        Page<Contract> result = contractRepository.findAllWithFilters(contractStatus, searchParam, pageable);

        return toSummaryPage(result);
    }

    /** SCR-38 — Manager list scoped to assigned properties (optional propertyId filter). */
    @Transactional(readOnly = true)
    public PageResponse<ContractSummaryResponse> getManagerContracts(
            User manager,
            String propertyIdStr,
            int page,
            int size,
            String status,
            String search,
            String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        Contract.Status contractStatus = parseStatus(status);
        String searchParam = cleanSearch(search);

        List<UUID> propertyIds;
        if (propertyIdStr != null && !propertyIdStr.isBlank()) {
            UUID propertyId = UUID.fromString(propertyIdStr.trim());
            scopeValidator.validateManagerAccess(manager, propertyId);
            propertyIds = List.of(propertyId);
        } else {
            propertyIds = assignmentRepository.findActivePropertiesByManagerId(manager.getId())
                    .stream()
                    .map(Property::getId)
                    .toList();
        }

        if (propertyIds.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }

        Page<Contract> result = contractRepository.findByPropertyIdsWithFilters(
                propertyIds, contractStatus, searchParam, pageable);
        return toSummaryPage(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractSummaryResponse> getMyContracts(User currentUser, int page, int size, String status, String search, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Contract.Status contractStatus = parseStatus(status);
        String searchParam = cleanSearch(search);

        Page<Contract> result = contractRepository.findByCustomerWithFilters(
                currentUser.getId(), contractStatus, searchParam, pageable);

        return toSummaryPage(result);
    }

    @Transactional(readOnly = true)
    public ContractDetailResponse getContractDetail(UUID id, User currentUser) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract không tồn tại"));
        assertCanViewContract(contract, currentUser);
        return ContractDetailResponse.fromEntity(contract);
    }

    @Transactional
    public ContractDetailResponse getOrCreateContractByBookingId(UUID bookingId, User currentUser) {
        com.homestay.entity.Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking không tồn tại"));

        if (currentUser.getRole() == User.Role.MANAGER) {
            scopeValidator.validateManagerAccess(currentUser, booking.getRoom().getProperty().getId());
        } else if (!booking.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Không có quyền xem hợp đồng này");
        }

        Contract contract = contractRepository.findByBookingId(bookingId)
                .orElseGet(() -> {
                    if (booking.getStatus() == com.homestay.entity.Booking.Status.PENDING_DEPOSIT ||
                        booking.getStatus() == com.homestay.entity.Booking.Status.CANCELLED) {
                        throw new IllegalArgumentException("Booking chưa xác nhận (CONFIRMED), chưa thể có hợp đồng");
                    }
                    Contract newContract = new Contract();
                    newContract.setBooking(booking);
                    newContract.setCustomer(booking.getCustomer());
                    newContract.setRoom(booking.getRoom());
                    newContract.setDepositAmount(booking.getDepositAmount() != null ? booking.getDepositAmount() : java.math.BigDecimal.ZERO);
                    newContract.setTotalAmount(booking.getTotalAmount() != null ? booking.getTotalAmount() : java.math.BigDecimal.ZERO);
                    newContract.setCheckInDate(booking.getCheckInDate());
                    newContract.setCheckOutDate(booking.getCheckOutDate());
                    newContract.setStatus(Contract.Status.ACTIVE);
                    newContract.setGeneratedAt(LocalDateTime.now());
                    Contract saved = contractRepository.save(newContract);

                    notificationService.sendNotification(
                            booking.getCustomer().getId(),
                            com.homestay.entity.Notification.Type.CONTRACT_GENERATED,
                            "Contract Generated",
                            "Your accommodation contract for booking has been generated and sent to your email.",
                            saved.getId(), "Contract"
                    );

                    return saved;
                });

        return ContractDetailResponse.fromEntity(contract);
    }

    @Transactional(readOnly = true)
    public byte[] downloadContractPdf(UUID id, User currentUser) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract không tồn tại"));
        assertCanViewContract(contract, currentUser);
        return pdfService.generateContractPdf(contract);
    }

    @Transactional
    public void resendContractEmail(UUID id, User currentUser, String targetEmail) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract không tồn tại"));

        if (currentUser.getRole() != User.Role.MANAGER) {
            throw new ForbiddenException("Chỉ Manager được gửi lại email hợp đồng");
        }
        scopeValidator.validateManagerAccess(currentUser, contract.getRoom().getProperty().getId());

        byte[] pdfBytes = pdfService.generateContractPdf(contract);

        String email = targetEmail != null && !targetEmail.isBlank() ? targetEmail : contract.getCustomer().getEmail();
        String subject = "Accommodation Contract - Booking #" + contract.getBooking().getId();
        String text = "Dear " + (contract.getCustomer().getFullName() != null ? contract.getCustomer().getFullName() : "Customer") + ",\n\n" +
                "Please find attached your accommodation contract for your upcoming stay at " +
                contract.getRoom().getProperty().getName() + ".\n\n" +
                "Thank you for choosing us!\n";

        emailService.sendEmailWithAttachment(email, subject, text, pdfBytes, "Contract_" + contract.getId() + ".pdf");

        contract.setSentAt(LocalDateTime.now());
        contractRepository.save(contract);
    }

    /** @deprecated Prefer {@link #resendContractEmail(UUID, User, String)} with RBAC. */
    @Transactional
    public void resendContractEmail(UUID id, String targetEmail) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract không tồn tại"));

        byte[] pdfBytes = pdfService.generateContractPdf(contract);

        String email = targetEmail != null && !targetEmail.isBlank() ? targetEmail : contract.getCustomer().getEmail();
        String subject = "Accommodation Contract - Booking #" + contract.getBooking().getId();
        String text = "Dear " + (contract.getCustomer().getFullName() != null ? contract.getCustomer().getFullName() : "Customer") + ",\n\n" +
                "Please find attached your accommodation contract for your upcoming stay at " +
                contract.getRoom().getProperty().getName() + ".\n\n" +
                "Thank you for choosing us!\n";

        emailService.sendEmailWithAttachment(email, subject, text, pdfBytes, "Contract_" + contract.getId() + ".pdf");

        contract.setSentAt(LocalDateTime.now());
        contractRepository.save(contract);
    }

    private void assertCanViewContract(Contract contract, User currentUser) {
        if (currentUser.getRole() == User.Role.MANAGER) {
            scopeValidator.validateManagerAccess(currentUser, contract.getRoom().getProperty().getId());
            return;
        }
        if (currentUser.getRole() == User.Role.ADMIN) {
            return;
        }
        if (!contract.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Không có quyền xem hợp đồng này");
        }
    }

    private PageResponse<ContractSummaryResponse> toSummaryPage(Page<Contract> result) {
        return new PageResponse<>(
                result.getContent().stream().map(ContractSummaryResponse::fromEntity).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private String cleanSearch(String search) {
        return (search != null && !search.isBlank()) ? search.trim() : null;
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "generatedAt"));
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        // Map API-friendly aliases to entity fields
        if ("createdAt".equals(field)) {
            field = "generatedAt";
        }
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    private Contract.Status parseStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) return null;
        try {
            return Contract.Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Transactional
    public void autoGenerateAndSendContract(UUID bookingId, User currentUser) {
        ContractDetailResponse contractResp = getOrCreateContractByBookingId(bookingId, currentUser);
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                resendContractEmail(contractResp.getId(), null);
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email hợp đồng (chưa cấu hình SMTP): " + e.getMessage());
            }
        });
    }
}
