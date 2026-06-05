package com.dormitory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatsDto {
    private long totalAvailableRooms;
    private long totalProperties;
    private long totalTenants;
    private int satisfactionPercent;
}
