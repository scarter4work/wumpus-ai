package org.scarter4work.wumpus2.controller;

import lombok.extern.slf4j.Slf4j;
import org.scarter4work.wumpus2.model.Game;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.service.GameService;
import org.scarter4work.wumpus2.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * REST Controller for game-related operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final RoomService roomService;

    @Autowired
    public GameController(GameService gameService, RoomService roomService) {
        this.gameService = gameService;
        this.roomService = roomService;
    }

    /**
     * Create a new game.
     *
     * @param request The request containing the player name
     * @return The created game
     */
    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody CreateGameRequest request) {
        Game game = gameService.createNewGame(request.getPlayerName());
        return ResponseEntity.ok(game);
    }

    /**
     * Get the current state of a game.
     *
     * @param gameId The ID of the game
     * @return The game state
     */
    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable UUID gameId) {
        Game game = gameService.getGame(gameId);
        Room currentRoom = gameService.getCurrentRoom(gameId);
        Set<UUID> visitedRooms = gameService.getVisitedRooms(gameId);
        Map<String, Boolean> hazardInfo = gameService.getHazardInformation(gameId);
        
        GameStateResponse response = new GameStateResponse(game, currentRoom, hazardInfo, visitedRooms);
        return ResponseEntity.ok(response);
    }

    /**
     * Move the player in a direction.
     *
     * @param gameId The ID of the game
     * @param request The request containing the direction to move
     * @return The updated game state
     */
    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameStateResponse> movePlayer(
            @PathVariable UUID gameId,
            @RequestBody MoveRequest request) {
        Game game = gameService.movePlayer(gameId, request.getDirection());
        Room currentRoom = gameService.getCurrentRoom(gameId);
        Set<UUID> visitedRooms = gameService.getVisitedRooms(gameId);
        Map<String, Boolean> hazardInfo = gameService.getHazardInformation(gameId);
        
        GameStateResponse response = new GameStateResponse(game, currentRoom, hazardInfo, visitedRooms);
        return ResponseEntity.ok(response);
    }

    /**
     * Shoot an arrow in a direction.
     *
     * @param gameId The ID of the game
     * @param request The request containing the direction to shoot
     * @return The updated game state
     */
    @PostMapping("/{gameId}/shoot")
    public ResponseEntity<GameStateResponse> shootArrow(
            @PathVariable UUID gameId,
            @RequestBody ShootRequest request) {
        Game game = gameService.shootArrow(gameId, request.getDirection());
        Room currentRoom = gameService.getCurrentRoom(gameId);
        Set<UUID> visitedRooms = gameService.getVisitedRooms(gameId);
        Map<String, Boolean> hazardInfo = gameService.getHazardInformation(gameId);
        
        GameStateResponse response = new GameStateResponse(game, currentRoom, hazardInfo, visitedRooms);
        return ResponseEntity.ok(response);
    }

    /**
     * Request class for creating a new game.
     */
    public static class CreateGameRequest {
        private String playerName;

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
    }

    /**
     * Request class for moving the player.
     */
    public static class MoveRequest {
        private String direction;

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }

    /**
     * Request class for shooting an arrow.
     */
    public static class ShootRequest {
        private String direction;

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }

    /**
     * Response class for game state.
     */
    public static class GameStateResponse {
        private Game game;
        private Room currentRoom;
        private Map<String, Boolean> hazardInfo;
        private Set<UUID> visitedRooms;

        public GameStateResponse(Game game, Room currentRoom, Map<String, Boolean> hazardInfo, Set<UUID> visitedRooms) {
            this.game = game;
            this.currentRoom = currentRoom;
            this.hazardInfo = hazardInfo;
            this.visitedRooms = visitedRooms;
        }

        public Game getGame() {
            return game;
        }

        public void setGame(Game game) {
            this.game = game;
        }

        public Room getCurrentRoom() {
            return currentRoom;
        }

        public void setCurrentRoom(Room currentRoom) {
            this.currentRoom = currentRoom;
        }

        public Map<String, Boolean> getHazardInfo() {
            return hazardInfo;
        }

        public void setHazardInfo(Map<String, Boolean> hazardInfo) {
            this.hazardInfo = hazardInfo;
        }

        public Set<UUID> getVisitedRooms() {
            return visitedRooms;
        }

        public void setVisitedRooms(Set<UUID> visitedRooms) {
            this.visitedRooms = visitedRooms;
        }
    }
}