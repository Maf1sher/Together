package org.mafisher.togetherbackend.mappers.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.mafisher.togetherbackend.dto.RoomDto;
import org.mafisher.togetherbackend.entity.Room;
import org.mafisher.togetherbackend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RoomMapper implements Mapper<Room, RoomDto> {
    private ModelMapper modelMapper;

    @Override
    public RoomDto mapTo(Room room) {
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public Room mapFrom(RoomDto roomDto) {
        return modelMapper.map(roomDto, Room.class);
    }
}
