package com.dormitory.controller;

import com.dormitory.dto.response.ApiResponse;
import com.dormitory.dto.response.FeaturedRoomDto;
import com.dormitory.dto.response.PlatformStatsDto;
import com.dormitory.service.PlatformStatsService;
import com.dormitory.service.PublicRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicRoomService publicRoomService;
    private final PlatformStatsService platformStatsService;

    @GetMapping("/rooms/featured")
    public ResponseEntity<ApiResponse<List<FeaturedRoomDto>>> getFeaturedRooms(
            @RequestParam(defaultValue = "6") int limit
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(publicRoomService.getFeaturedRooms(limit))
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PlatformStatsDto>> getStats() {
        return ResponseEntity.ok(
                ApiResponse.ok(platformStatsService.getStats())
        );
    }
}
