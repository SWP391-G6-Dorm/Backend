package com.homestay.service;

import com.homestay.dto.request.EmployeeInspectionResultRequest;
import com.homestay.dto.response.EmployeeInspectionResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.EmployeePropertyAssignment;
import com.homestay.entity.RoomInspection;
import com.homestay.entity.User;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.EmployeePropertyAssignmentRepository;
import com.homestay.repository.RoomInspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SCR-62 - Employee Room Inspection Hub.
 * List PENDING/IN_PROGRESS inspections in ACTIVE properties; pass or fail by inspection id.
 * Pass/Fail allowed only when unclaimed (PENDING / no inspector) or claimed by current employee.
 */
@Service
@RequiredArgsConstructor
public class EmployeeInspectionService {

    private static final List<RoomInspection.Status> OPEN_STATUSES =
            List.of(RoomInspection.Status.PENDING, RoomInspection.Status.IN_PROGRESS);

    private final RoomInspectionRepository roomInspectionRepository;
    private final EmployeePropertyAssignmentRepository employeePropertyAssignmentRepository;

    @Transactional(readOnly = true)
    public PageResponse<EmployeeInspectionResponse> list(User employee, String status, Pageable pageable) {
        List<UUID> propertyIds = activePropertyIds(employee.getId());
        if (propertyIds.isEmpty()) {
            return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
        }

        List<RoomInspection.Status> statuses = resolveStatuses(status);
        Page<RoomInspection> page = roomInspectionRepository.findForEmployee(propertyIds, statuses, pageable);
        List<EmployeeInspectionResponse> content = page.getContent().stream()
                .map(EmployeeInspectionResponse::fromEntity)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional
    public void pass(User employee, UUID id, EmployeeInspectionResultRequest request) {
        RoomInspection inspection = loadInScope(employee.getId(), id);
        guardCanPerform(inspection, employee);
        inspection.setStatus(RoomInspection.Status.PASSED);
        inspection.setInspectedBy(employee);
        inspection.setInspectedAt(LocalDateTime.now());
        inspection.setNote(buildNote(request, false));
        roomInspectionRepository.save(inspection);
    }

    @Transactional
    public void fail(User employee, UUID id, EmployeeInspectionResultRequest request) {
        if (request == null || !StringUtils.hasText(request.resolveNote())) {
            throw new BusinessException("Please describe the damage found");
        }
        RoomInspection inspection = loadInScope(employee.getId(), id);
        guardCanPerform(inspection, employee);
        inspection.setStatus(RoomInspection.Status.FAILED_WITH_DAMAGE);
        inspection.setInspectedBy(employee);
        inspection.setInspectedAt(LocalDateTime.now());
        inspection.setNote(buildNote(request, true));
        roomInspectionRepository.save(inspection);
    }

    private List<RoomInspection.Status> resolveStatuses(String status) {
        if (!StringUtils.hasText(status)) {
            return OPEN_STATUSES;
        }
        try {
            RoomInspection.Status parsed = RoomInspection.Status.valueOf(status.trim().toUpperCase());
            if (!OPEN_STATUSES.contains(parsed)) {
                throw new BusinessException("status filter must be PENDING or IN_PROGRESS");
            }
            return List.of(parsed);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid inspection status: " + status);
        }
    }

    private List<UUID> activePropertyIds(UUID employeeId) {
        return employeePropertyAssignmentRepository
                .findPropertyIdsByEmployeeIdAndStatus(employeeId, EmployeePropertyAssignment.Status.ACTIVE);
    }

    private RoomInspection loadInScope(UUID employeeId, UUID inspectionId) {
        List<UUID> propertyIds = activePropertyIds(employeeId);
        if (propertyIds.isEmpty()) {
            throw new ResourceNotFoundException("Khong tim thay room inspection");
        }
        return roomInspectionRepository.findByIdAndPropertyIdIn(inspectionId, propertyIds)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay room inspection"));
    }

    private void guardCanPerform(RoomInspection inspection, User employee) {
        RoomInspection.Status status = inspection.getStatus();
        if (status != RoomInspection.Status.PENDING && status != RoomInspection.Status.IN_PROGRESS) {
            throw new BusinessException("Inspection da hoan tat");
        }
        User holder = inspection.getInspectedBy();
        if (status == RoomInspection.Status.IN_PROGRESS
                && holder != null
                && !holder.getId().equals(employee.getId())) {
            throw new BusinessException("Pass/Fail chi khi inspection da gan cho ban");
        }
    }

    private String buildNote(EmployeeInspectionResultRequest request, boolean fail) {
        if (request == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String note = request.resolveNote();
        if (StringUtils.hasText(note)) {
            sb.append(note);
        }
        EmployeeInspectionResultRequest.Checklist c = request.getChecklist();
        if (c != null) {
            List<String> parts = new ArrayList<>();
            appendCheck(parts, "tv", c.getTv());
            appendCheck(parts, "minibar", c.getMinibar());
            appendCheck(parts, "ac", c.getAc());
            appendCheck(parts, "bathroom", c.getBathroom());
            appendCheck(parts, "beds", c.getBeds());
            if (!parts.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append("Checklist: ").append(String.join(", ", parts));
            }
        }
        if (fail && sb.length() == 0) {
            throw new BusinessException("Please describe the damage found");
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private void appendCheck(List<String> parts, String name, Boolean ok) {
        if (ok == null) {
            return;
        }
        parts.add(name + "=" + (ok ? "OK" : "FAIL"));
    }
}
