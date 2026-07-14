package com.homestay.service;

import com.homestay.dto.request.CreateEmployeeDamageReportRequest;
import com.homestay.dto.response.EmployeeDamageReportResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.Attachment;
import com.homestay.entity.DamageItem;
import com.homestay.entity.DamageReport;
import com.homestay.entity.RoomInspection;
import com.homestay.entity.User;
import com.homestay.exception.BusinessException;
import com.homestay.repository.AttachmentRepository;
import com.homestay.repository.DamageReportRepository;
import com.homestay.repository.RoomInspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SCR-63 - Employee Damage Report List.
 * SCR-64 - Create Damage Report (resolve inspection from roomId + employee).
 * Own reports = reports whose inspection.inspectedBy = current employee.
 */
@Service
@RequiredArgsConstructor
public class EmployeeDamageReportService {

    private static final BigDecimal ESCALATION_THRESHOLD = new BigDecimal("5000000");

    private final DamageReportRepository damageReportRepository;
    private final RoomInspectionRepository roomInspectionRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional(readOnly = true)
    public PageResponse<EmployeeDamageReportResponse> list(User employee, Pageable pageable) {
        Page<DamageReport> page = damageReportRepository.findForEmployee(employee.getId(), pageable);
        List<EmployeeDamageReportResponse> content = page.getContent().stream()
                .map(EmployeeDamageReportResponse::fromEntity)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional
    public EmployeeDamageReportResponse create(User employee, CreateEmployeeDamageReportRequest req) {
        RoomInspection inspection = resolveInspection(employee, req.getRoomId());

        BigDecimal total = req.getItems().stream()
                .map(CreateEmployeeDamageReportRequest.Item::getEstimatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DamageReport report = new DamageReport();
        report.setInspection(inspection);
        report.setBooking(inspection.getBooking());
        report.setStatus(DamageReport.Status.PENDING_APPROVAL);
        report.setTotalEstimatedCost(total);
        report.setRequiresAdminEscalation(total.compareTo(ESCALATION_THRESHOLD) > 0);
        report.setNote(req.getNotes());
        report.setItems(new ArrayList<>());

        for (CreateEmployeeDamageReportRequest.Item i : req.getItems()) {
            DamageItem di = new DamageItem();
            di.setDamageReport(report);
            di.setItemName(i.getName());
            di.setEstimatedCost(i.getEstimatedCost());
            report.getItems().add(di);
        }

        DamageReport saved = damageReportRepository.save(report);

        if (req.getAttachments() != null && !req.getAttachments().isEmpty()
                && saved.getItems() != null && !saved.getItems().isEmpty()) {
            DamageItem firstItem = saved.getItems().get(0);
            List<Attachment> attachments = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            for (CreateEmployeeDamageReportRequest.AttachmentRef ref : req.getAttachments()) {
                Attachment a = new Attachment();
                a.setEntityType("DamageItem");
                a.setEntityId(firstItem.getId());
                a.setFileUrl(ref.getUrl());
                a.setUploadedAt(now);
                attachments.add(a);
            }
            attachmentRepository.saveAll(attachments);
        }

        return EmployeeDamageReportResponse.fromEntity(saved);
    }

    private RoomInspection resolveInspection(User employee, java.util.UUID roomId) {
        List<RoomInspection> candidates = roomInspectionRepository.findFailedForEmployeeAndRoom(
                roomId,
                employee.getId(),
                RoomInspection.Status.FAILED_WITH_DAMAGE,
                PageRequest.of(0, 5));
        for (RoomInspection ri : candidates) {
            if (!damageReportRepository.existsByInspection_Id(ri.getId())) {
                return ri;
            }
        }
        throw new BusinessException(
                "Khong tim thay inspection FAILED chua co bao cao cho phong nay");
    }
}