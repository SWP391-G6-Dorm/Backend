package com.homestay.dto.request;

import com.homestay.entity.MaintenanceTicket;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMaintenanceStatusRequest {

    @NotNull(message = "Trạng thái là bắt buộc")
    private MaintenanceTicket.Status status;

    private String resolutionNote;
}
