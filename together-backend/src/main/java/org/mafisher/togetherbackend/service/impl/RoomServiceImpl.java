package org.mafisher.togetherbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.mafisher.togetherbackend.dto.RoomDto;
import org.mafisher.togetherbackend.dto.request.CreateRoomRequest;
import org.mafisher.togetherbackend.entity.Room;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.handler.BusinessErrorCodes;
import org.mafisher.togetherbackend.handler.CustomException;
import org.mafisher.togetherbackend.mappers.Mapper;
import org.mafisher.togetherbackend.repository.FriendshipRepository;
import org.mafisher.togetherbackend.repository.RoomRepository;
import org.mafisher.togetherbackend.service.PrincipalService;
import org.mafisher.togetherbackend.service.RoomService;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final PrincipalService principalService;
    private final Mapper<Room, RoomDto> roomMapper;
    private final FriendshipRepository friendshipRepository;

    @Override
    public RoomDto createRoom(CreateRoomRequest createRoomRequest, Principal principal) {
        User user = principalService.checkUserPrincipal(principal);

        if(roomRepository.findByOwnerAndName(user, createRoomRequest.getName()).isPresent())
            throw new CustomException(BusinessErrorCodes.ROOM_NAME_TAKEN);

        Room room = Room.builder()
                .name(createRoomRequest.getName())
                .owner(user)
                .participants(new HashSet<>(List.of(user)))
                .build();

        return roomMapper.mapTo(roomRepository.save(room));
    }

    @Override
    public void deleteRoom(Long roomId, Principal principal) {
        User user = principalService.checkUserPrincipal(principal);
        Room room = checkRoomExists(roomId);
        checkRoomOwner(room, user);

        roomRepository.delete(room);
    }

    @Override
    public RoomDto addParticipant(Long roomId, String username, Principal principal) {
        User owner = principalService.checkUserPrincipal(principal);
        Room room = checkRoomExists(roomId);
        checkRoomOwner(room, owner);
        User participant = principalService.checkUserExist(username);

        if(owner.getId().equals(participant.getId()))
            throw new CustomException(BusinessErrorCodes.USERS_ARE_THE_SAME);

        if(!friendshipRepository.existsBetweenUsers(owner, participant))
            throw new CustomException(BusinessErrorCodes.USERS_NOT_FRIENDS);

        if(roomRepository.isUserInRoom(room, participant))
            throw new CustomException(BusinessErrorCodes.USER_ALREADY_IN_ROOM);

        room.addParticipant(participant);
        return roomMapper.mapTo(roomRepository.save(room));
    }

    @Override
    public RoomDto removeParticipant(Long roomId, String username, Principal principal) {
        User owner = principalService.checkUserPrincipal(principal);
        Room room = checkRoomExists(roomId);
        checkRoomOwner(room, owner);
        User participant = principalService.checkUserExist(username);

        if(!roomRepository.isUserInRoom(room, participant))
            throw new CustomException(BusinessErrorCodes.USER_NOT_IN_ROOM);

        if(owner.getId().equals(participant.getId()))
            throw new CustomException(BusinessErrorCodes.PARTICIPANT_IS_OWNER);

        room.removeParticipant(participant);
        return roomMapper.mapTo(roomRepository.save(room));
    }

    @Override
    public List<RoomDto> getOwnedRooms(Principal principal) {
        User user = principalService.checkUserPrincipal(principal);
        return roomRepository.findByOwner(user).stream()
                .map(roomMapper::mapTo).toList();
    }

    @Override
    public List<RoomDto> getBelongsRooms( Principal principal) {
        User user = principalService.checkUserPrincipal(principal);
        return roomRepository.findRoomsByUser(user).stream()
                .map(roomMapper::mapTo).toList();
    }

    private boolean checkRoomOwner(Room room, User user) {
        if(!room.getOwner().equals(user))
            throw new CustomException(BusinessErrorCodes.NOT_OWN_ROOM);
        return true;
    }

    private Room checkRoomExists(Long roomId) {
        return roomRepository.findById(roomId).orElseThrow(
                () -> new CustomException(BusinessErrorCodes.ROOM_NOT_FOUND)
        );
    }

}
