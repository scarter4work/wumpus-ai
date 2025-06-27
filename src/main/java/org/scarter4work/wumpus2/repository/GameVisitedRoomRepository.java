package org.scarter4work.wumpus2.repository;

import org.scarter4work.wumpus2.model.GameVisitedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for GameVisitedRoom entity operations.
 */
@Repository
public interface GameVisitedRoomRepository extends JpaRepository<GameVisitedRoom, UUID> {

    /**
     * Find all visited rooms for a specific game.
     *
     * @param gameId The ID of the game
     * @return List of GameVisitedRoom records for the game
     */
    List<GameVisitedRoom> findByGameId(UUID gameId);

    /**
     * Find all games where a specific room has been visited.
     *
     * @param roomId The ID of the room
     * @return List of GameVisitedRoom records for the room
     */
    List<GameVisitedRoom> findByRoomId(UUID roomId);

    /**
     * Check if a specific room has been visited in a specific game.
     *
     * @param gameId The ID of the game
     * @param roomId The ID of the room
     * @return true if the room has been visited in the game, false otherwise
     */
    boolean existsByGameIdAndRoomId(UUID gameId, UUID roomId);

    /**
     * Find a specific visited room record by game ID and room ID.
     *
     * @param gameId The ID of the game
     * @param roomId The ID of the room
     * @return The GameVisitedRoom record if found
     */
    GameVisitedRoom findByGameIdAndRoomId(UUID gameId, UUID roomId);

    /**
     * Delete all visited room records for a specific game.
     *
     * @param gameId The ID of the game
     */
    void deleteByGameId(UUID gameId);

    /**
     * Count the number of visited rooms for a specific game.
     *
     * @param gameId The ID of the game
     * @return The count of visited rooms
     */
    long countByGameId(UUID gameId);
}
