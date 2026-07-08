package com.homestay.service;

import com.homestay.dto.request.UpdateMaintenanceStatusRequest;
import com.homestay.dto.response.EmployeeMaintenanceTicketResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.MaintenanceTicket;
import com.homestay.entity.User;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.MaintenanceTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * SCR-61 - Employee Maintenance Workspace.
 * List assigned tickets; update status ASSIGNED -> IN_PROGRESS -> RESOLVED only.
 */
@Service
@RequiredArgsConstructor
public class EmployeeMaintenanceService {

    private final MaintenanceTicketRepository maintenanceTicketRepository;

    @Transactional(readOnly = true)
    public PageResponse<EmployeeMaintenanceTicketResponse> list(
            User employee, String statusStr, Pageable pageable) {
        MaintenanceTicket.Status status = parseStatus(statusStr);
        Page<MaintenanceTicket> page = maintenanceTicketRepository.findForEmployee(
                employee.getId(), status, pageable);
        List<EmployeeMaintenanceTicketResponse> content = page.getContent().stream()
                .map(EmployeeMaintenanceTicketResponse::fromEntity)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional
    public void updateStatus(User employee, UUID ticketId, UpdateMaintenanceStatusRequest request) {
        MaintenanceTicket ticket = maintenanceTicketRepository
                .findByIdAndAssignedEmployeeId(ticketId, employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay maintenance ticket"));

        MaintenanceTicket.Status current = ticket.getStatus();
        MaintenanceTicket.Status next = request.getStatus();

        boolean allowed =
                (current == MaintenanceTicket.Status.ASSIGNED
                        && next == MaintenanceTicket.Status.IN_PROGRESS)
                || (current == MaintenanceTicket.Status.IN_PROGRESS
                        && next == MaintenanceTicket.Status.RESOLVED);

        if (!allowed) {
            throw new BusinessException("Chuyen trang thai khong hop le");
        }

        ticket.setStatus(next);
        if (next == MaintenanceTicket.Status.RESOLVED
                && StringUtils.hasText(request.getResolutionNote())) {
            ticket.setResolutionNote(request.getResolutionNote());
        }
        maintenanceTicketRepository.save(ticket);
    }

    private MaintenanceTicket.Status parseStatus(String statusStr) {
        if (!StringUtils.hasText(statusStr)) {
            return null;
        }
        try {
            return MaintenanceTicket.Status.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Trang thai khong hop le");
        }
    }
}