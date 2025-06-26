package org.scarter4work.wumpus2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

/**
 * Represents a mapping between a Game and a Room.
 * This lookup table allows rooms to be associated with games without direct foreign keys.
 */
@Entity
@Table(name = "game_rooms")
@Data
@NoArgsConstructor
@Slf4j
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID gameId;

    @Column(nullable = false)
    private UUID roomId;

    /**
     * Creates a new GameRoom mapping.
     *
     * @param gameId The ID of the game
     * @param roomId The ID of the room
     * @return A new GameRoom instance
     */
    public static GameRoom createGameRoom(UUID gameId, UUID roomId) {
        log.info("Creating new GameRoom mapping between game: {} and room: {}", gameId, roomId);
        GameRoom gameRoom = new GameRoom();
        gameRoom.setGameId(gameId);
        gameRoom.setRoomId(roomId);
        log.info("GameRoom mapping created");
        return gameRoom;
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
}
