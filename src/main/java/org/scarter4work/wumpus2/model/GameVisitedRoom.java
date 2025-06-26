package org.scarter4work.wumpus2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a room that has been visited by a player in a specific game.
 * This entity tracks which rooms have been visited and when.
 */
@Entity
@Table(name = "game_visited_rooms")
@Data
@NoArgsConstructor
@Slf4j
public class GameVisitedRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID gameId;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    /**
     * Creates a new GameVisitedRoom record.
     *
     * @param gameId The ID of the game
     * @param roomId The ID of the room that was visited
     * @return A new GameVisitedRoom instance
     */
    public static GameVisitedRoom createVisitedRoom(UUID gameId, UUID roomId) {
        log.info("Recording visit to room: {} in game: {}", roomId, gameId);
        GameVisitedRoom visitedRoom = new GameVisitedRoom();
        visitedRoom.setGameId(gameId);
        visitedRoom.setRoomId(roomId);
        LocalDateTime now = LocalDateTime.now();
        visitedRoom.setVisitedAt(now);
        log.info("Room visit recorded at: {}", now);
        return visitedRoom;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getVisitedAt() {
        return visitedAt;
    }

    public void setVisitedAt(LocalDateTime visitedAt) {
        this.visitedAt = visitedAt;
    }
}
