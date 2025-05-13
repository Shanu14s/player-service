package com.example.plareyservice2.service;


import com.example.plareyservice2.dto.LoginRequest;
import com.example.plareyservice2.dto.PlayerRegistrationRequest;
import com.example.plareyservice2.dto.TimeLimitRequest;
import com.example.plareyservice2.entity.Player;
import com.example.plareyservice2.entity.Session;
import com.example.plareyservice2.exception.PlayerInactiveException;
import com.example.plareyservice2.repository.PlayerRepository;
import com.example.plareyservice2.repository.SessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PlayerService {

    private final PlayerRepository playerRepo;
    private final SessionRepository sessionRepo;
    private final PasswordEncoder passwordEncoder;

    public PlayerService(PlayerRepository playerRepo, SessionRepository sessionRepo, PasswordEncoder passwordEncoder) {
        this.playerRepo = playerRepo;
        this.sessionRepo = sessionRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Player register(PlayerRegistrationRequest request) {
        Player player = new Player();
        player.setEmail(request.email);
        player.setPassword(passwordEncoder.encode(request.password));
        player.setName(request.name);
        player.setSurname(request.surname);
        player.setDob(LocalDate.parse(request.dob));
        player.setAddress(request.address);
        return playerRepo.save(player);
    }

    public UUID login(LoginRequest request) {
        Player player = playerRepo.findByEmail(request.email)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (!passwordEncoder.matches(request.password, player.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Duration todayDuration = sessionRepo.findByPlayerId(player.getId()).stream()
                .filter(s -> s.getLogoutTimestamp() != null && s.getLoginTimestamp().toLocalDate().equals(LocalDate.now()))
                .map(s -> Duration.between(s.getLoginTimestamp(), s.getLogoutTimestamp()))
                .reduce(Duration.ZERO, Duration::plus);

        if (player.getDailyLimitMinutes() != null && todayDuration.toMinutes() >= player.getDailyLimitMinutes()) {
            throw new RuntimeException("Daily time limit reached");
        }

        Session session = new Session();
        session.setPlayerId(player.getId());
        session.setLoginTimestamp(LocalDateTime.now());
        session = sessionRepo.save(session);

        return session.getId();
    }

    public void logout(UUID sessionId) {
        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setLogoutTimestamp(LocalDateTime.now());
        sessionRepo.save(session);
    }

    public void setTimeLimit(UUID playerId, TimeLimitRequest request) {
        boolean isActive = sessionRepo.existsByPlayerIdAndLogoutTimestampIsNull(playerId);
        if (!isActive) throw new PlayerInactiveException("Cannot set time limit for inactive player");

        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setDailyLimitMinutes(request.dailyLimitMinutes);
        playerRepo.save(player);
    }
}
