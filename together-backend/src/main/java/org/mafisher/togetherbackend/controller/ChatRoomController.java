package org.mafisher.togetherbackend.controller;

import lombok.RequiredArgsConstructor;
import org.mafisher.togetherbackend.dto.RoomDto;
import org.mafisher.togetherbackend.dto.request.CreateRoomRequest;
import org.mafisher.togetherbackend.service.AuthService;
import org.mafisher.togetherbackend.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<RoomDto> createRoom(@RequestBody CreateRoomRequest request, Principal principal) {
        return new ResponseEntity<>(roomService.createRoom(request, principal), HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<?> deleteRoom(
            @PathVariable Long roomId,
            Principal principal) {
        roomService.deleteRoom(roomId, principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-participant/{roomId}/{username}")
    public ResponseEntity<RoomDto> addParticipant(
            @PathVariable Long roomId,
            @PathVariable String username,
            Principal principal) {
        return new ResponseEntity<>(roomService.addParticipant(roomId, username, principal), HttpStatus.OK);
    }

    @PostMapping("/remove-participant/{roomId}/{username}")
    public ResponseEntity<RoomDto> removeParticipant(
            @PathVariable Long roomId,
            @PathVariable String username,
            Principal principal) {
        return new ResponseEntity<>(roomService.removeParticipant(roomId, username, principal), HttpStatus.OK);
    }

    @GetMapping("/get-owned-rooms")
    public ResponseEntity<List<RoomDto>> getOwnedRooms(Principal principal) {
        return new ResponseEntity<>(roomService.getOwnedRooms(principal), HttpStatus.OK);
    }

    @GetMapping("/get-belongs-rooms")
    public ResponseEntity<List<RoomDto>> getBelongsRooms(Principal principal) {
        return new ResponseEntity<>(roomService.getBelongsRooms(principal), HttpStatus.OK);
    }
}
