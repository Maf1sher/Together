package org.mafisher.togetherbackend.controller;

import lombok.RequiredArgsConstructor;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.service.FriendService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/requests/{nickname}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendRequest(@PathVariable String nickname, Principal principal) {
        friendService.sendRequest(nickname, principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{nickname}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> acceptRequest(@PathVariable String nickname, Principal principal) {
        friendService.acceptRequest(nickname, principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{nickname}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> rejectRequest(@PathVariable String nickname, Principal principal) {
        friendService.rejectRequest(nickname, principal);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDto>> getFriends(Principal principal, Pageable pageable) {
        return new ResponseEntity<>(friendService.getFriends(principal, pageable), HttpStatus.OK);
    }
}
