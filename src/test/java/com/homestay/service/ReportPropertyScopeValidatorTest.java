package com.homestay.service;

import com.homestay.entity.User;
import com.homestay.exception.ForbiddenException;
import com.homestay.repository.ManagerPropertyAssignmentRepository;
import com.homestay.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportPropertyScopeValidatorTest {

    @Mock ManagerPropertyAssignmentRepository assignmentRepository;
    @InjectMocks ReportPropertyScopeValidator validator;

    @Test
    void validateManagerAccess_assigned_ok() {
        User manager = TestFixtures.user(User.Role.MANAGER);
        UUID propertyId = UUID.randomUUID();
        when(assignmentRepository.existsByManagerIdAndPropertyIdAndStatus(
                manager.getId(), propertyId, com.homestay.entity.ManagerPropertyAssignment.Status.ACTIVE))
                .thenReturn(true);

        assertDoesNotThrow(() -> validator.validateManagerAccess(manager, propertyId));
    }

    @Test
    void validateManagerAccess_unassigned_forbidden() {
        User manager = TestFixtures.user(User.Role.MANAGER);
        UUID propertyId = UUID.randomUUID();
        when(assignmentRepository.existsByManagerIdAndPropertyIdAndStatus(
                manager.getId(), propertyId, com.homestay.entity.ManagerPropertyAssignment.Status.ACTIVE))
                .thenReturn(false);

        assertThrows(ForbiddenException.class, () -> validator.validateManagerAccess(manager, propertyId));
    }

    @Test
    void validateManagerAccess_nullManager_forbidden() {
        assertThrows(ForbiddenException.class,
                () -> validator.validateManagerAccess(null, UUID.randomUUID()));
    }
}
