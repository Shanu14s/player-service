package com.example.plareyservice2.repository;

import com.example.plareyservice2.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByPlayerId(UUID playerId);
    boolean existsByPlayerIdAndLogoutTimestampIsNull(UUID playerId);
    List<Session> findByPlayerIdAndLogoutTimestampIsNotNull(UUID playerId);
}
