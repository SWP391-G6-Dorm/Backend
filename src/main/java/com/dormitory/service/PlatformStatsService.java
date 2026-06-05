package com.dormitory.service;

import com.dormitory.dto.response.PlatformStatsDto;
import com.dormitory.entity.Room;
import com.dormitory.entity.User;
import com.dormitory.repository.PropertyRepository;
import com.dormitory.repository.RoomRepository;
import com.dormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformStatsService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PlatformStatsDto getStats() {
        long availableRooms = roomRepository.countByStatus(Room.Status.AVAILABLE);
        long properties = propertyRepository.count();
        long tenants = userRepository.countByRole(User.Role.TENANT);
        // Satisfaction percent is mocked for now
        int satisfactionPercent = 98;
        return new PlatformStatsDto(availableRooms, properties, tenants, satisfactionPercent);
    }
}
