package com.example.plareyservice2.controller;
import com.example.plareyservice2.dto.LoginRequest;
import com.example.plareyservice2.dto.PlayerRegistrationRequest;
import com.example.plareyservice2.dto.TimeLimitRequest;
import com.example.plareyservice2.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody PlayerRegistrationRequest request) {
        return ResponseEntity.ok(playerService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UUID sessionId = playerService.login(request);
        return ResponseEntity.ok().body("Session ID: " + sessionId);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam UUID sessionId) {
        playerService.logout(sessionId);
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/{playerId}/limit")
    public ResponseEntity<?> setTimeLimit(@PathVariable UUID playerId, @RequestBody TimeLimitRequest request) {
        playerService.setTimeLimit(playerId, request);
        return ResponseEntity.ok("Limit set");
    }
}
