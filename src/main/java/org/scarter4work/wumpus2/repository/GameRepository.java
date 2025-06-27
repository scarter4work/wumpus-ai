package org.scarter4work.wumpus2.repository;

import org.scarter4work.wumpus2.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing and manipulating Game entities.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    
    /**
     * Find all games for a specific player.
     * 
     * @param playerName The name of the player
     * @return List of games for the player
     */
    List<Game> findByPlayerName(String playerName);
    
    /**
     * Find all games with a specific status.
     * 
     * @param status The game status
     * @return List of games with the specified status
     */
    List<Game> findByStatus(Game.GameStatus status);
    
    /**
     * Find all in-progress games for a specific player.
     * 
     * @param playerName The name of the player
     * @return List of in-progress games for the player
     */
    List<Game> findByPlayerNameAndStatus(String playerName, Game.GameStatus status);
}