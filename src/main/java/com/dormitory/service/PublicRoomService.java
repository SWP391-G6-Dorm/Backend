package com.dormitory.service;

import com.dormitory.dto.response.FeaturedRoomDto;
import com.dormitory.entity.Room;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicRoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public List<FeaturedRoomDto> getFeaturedRooms(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Room> rooms = roomRepository.findFeaturedRooms(pageable);
        
        List<FeaturedRoomDto> featuredRooms = new ArrayList<>();
        for (Room room : rooms) {
            featuredRooms.add(roomMapper.toFeaturedRoomDto(room));
        }
        
        return featuredRooms;
    }
}
