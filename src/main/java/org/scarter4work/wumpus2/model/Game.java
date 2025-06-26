package org.scarter4work.wumpus2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a game session of Hunt the Wumpus.
 */
@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@Slf4j
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Column(nullable = false)
    private Integer arrowsRemaining = 5;

    @Column(nullable = false)
    private UUID currentRoomId;

    public enum GameStatus {
        IN_PROGRESS,
        WON,
        LOST
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Integer getArrowsRemaining() {
        return arrowsRemaining;
    }

    public void setArrowsRemaining(Integer arrowsRemaining) {
        this.arrowsRemaining = arrowsRemaining;
    }

    public UUID getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(UUID currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    /**
     * Creates a new game for the given player.
     * 
     * @param playerName The name of the player
     * @param startingRoomId The ID of the room where the player starts
     * @return A new Game instance
     */
    public static Game createNewGame(String playerName, UUID startingRoomId) {
        log.info("Creating new game for player: {} starting in room: {}", playerName, startingRoomId);
        Game game = new Game();
        game.setPlayerName(playerName);
        game.setStartTime(LocalDateTime.now());
        game.setCurrentRoomId(startingRoomId);
        log.info("New game created at: {}", game.getStartTime());
        return game;
    }
}
