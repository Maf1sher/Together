package org.mafisher.togetherbackend.service;


import org.mafisher.togetherbackend.dto.RoomDto;
import org.mafisher.togetherbackend.dto.request.CreateRoomRequest;

import java.security.Principal;
import java.util.List;

public interface RoomService {
    RoomDto createRoom(CreateRoomRequest createRoomRequest, Principal principal);
    void deleteRoom(Long roomId, Principal principal);
    RoomDto addParticipant(Long roomId, String username, Principal principal);
    RoomDto removeParticipant(Long roomId, String username, Principal principal);
    List<RoomDto> getOwnedRooms(Principal principal);
    List<RoomDto> getBelongsRooms(Principal principal);
}
