package com.homestay.service;

import com.homestay.dto.response.AdminComplaintResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.Complaint;
import com.homestay.entity.Notification;
import com.homestay.entity.User;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SCR-54 - Complaint Management (Admin). List + resolve.
 * KHONG dung ComplaintService (Manager/Customer).
 */
@Service
@RequiredArgsConstructor
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<AdminComplaintResponse> listComplaints(String statusStr, Pageable pageable) {
        Complaint.Status status = parseStatus(statusStr);
        Page<Complaint> page = complaintRepository.findForAdmin(status, pageable);
        return new PageResponse<>(
                page.getContent().stream().map(AdminComplaintResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional
    public AdminComplaintResponse resolve(UUID id, String resolution, User admin) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khieu nai"));

        if (c.getStatus() == Complaint.Status.RESOLVED || c.getStatus() == Complaint.Status.CLOSED) {
            throw new BusinessException("Khieu nai da duoc xu ly");
        }

        c.setStatus(Complaint.Status.RESOLVED);
        c.setResolutionNotes(resolution.trim());
        if (c.getResolvedAt() == null) {
            c.setResolvedAt(LocalDateTime.now());
        }

        Complaint saved = complaintRepository.save(c);
        notifyCustomer(saved);
        return AdminComplaintResponse.from(saved);
    }

    private Complaint.Status parseStatus(String statusStr) {
        if (statusStr == null || statusStr.isBlank() || "ALL".equalsIgnoreCase(statusStr.trim())) {
            return null;
        }
        try {
            return Complaint.Status.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Trang thai khong hop le");
        }
    }

    private void notifyCustomer(Complaint c) {
        if (c.getUser() != null) {
            notificationService.sendNotification(
                    c.getUser().getId(),
                    Notification.Type.SYSTEM,
                    "Cap nhat khieu nai",
                    "Khieu nai cua ban da duoc xu ly.",
                    c.getId(),
                    "Complaint");
        }
    }
}