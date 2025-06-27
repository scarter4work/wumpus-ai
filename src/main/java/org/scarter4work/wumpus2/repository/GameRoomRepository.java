package org.scarter4work.wumpus2.repository;

import org.scarter4work.wumpus2.model.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for GameRoom entity operations.
 */
@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, UUID> {
    
    /**
     * Find all GameRoom mappings for a specific game.
     * 
     * @param gameId The ID of the game
     * @return List of GameRoom mappings for the game
     */
    List<GameRoom> findByGameId(UUID gameId);
    
    /**
     * Find all GameRoom mappings for a specific room.
     * 
     * @param roomId The ID of the room
     * @return List of GameRoom mappings for the room
     */
    List<GameRoom> findByRoomId(UUID roomId);
    
    /**
     * Find a specific GameRoom mapping by game ID and room ID.
     * 
     * @param gameId The ID of the game
     * @param roomId The ID of the room
     * @return The GameRoom mapping if found
     */
    GameRoom findByGameIdAndRoomId(UUID gameId, UUID roomId);
    
    /**
     * Delete all GameRoom mappings for a specific game.
     * 
     * @param gameId The ID of the game
     */
    void deleteByGameId(UUID gameId);
}