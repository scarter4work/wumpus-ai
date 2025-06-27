package org.scarter4work.wumpus2.controller;

import lombok.extern.slf4j.Slf4j;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for room-related operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Get all rooms for a specific game.
     *
     * @param gameId The ID of the game
     * @return List of rooms in the game
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<Room>> getRoomsForGame(@PathVariable UUID gameId) {
        log.info("Getting rooms for game: {}", gameId);
        List<Room> rooms = roomService.getRoomsForGame(gameId);
        log.info("Found {} rooms for game: {}", rooms.size(), gameId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Place hazards for a specific game.
     *
     * @param gameId The ID of the game
     * @param request The request containing hazard counts
     * @return Success status
     */
    @PostMapping("/game/{gameId}/hazards")
    public ResponseEntity<Boolean> placeHazards(
            @PathVariable UUID gameId,
            @RequestBody PlaceHazardsRequest request) {

        log.info("Placing hazards for game: {}, wumpus: {}, pits: {}, bats: {}", 
                gameId, request.getWumpusCount(), request.getPitCount(), request.getBatCount());

        // For simplicity, we'll use random placement
        boolean success = roomService.placeRandomHazards(gameId);

        if (success) {
            log.info("Successfully placed hazards for game: {}", gameId);
        } else {
            log.error("Failed to place hazards for game: {}", gameId);
        }

        return ResponseEntity.ok(success);
    }

    /**
     * Get a specific room.
     *
     * @param roomId The ID of the room
     * @return The room
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable UUID roomId) {
        log.info("Getting room with ID: {}", roomId);
        Room room = roomService.getRoom(roomId);
        log.info("Found room: {}", room.getId());
        return ResponseEntity.ok(room);
    }

    /**
     * Get all rooms with the Wumpus.
     *
     * @return List of rooms with the Wumpus
     */
    @GetMapping("/wumpus")
    public ResponseEntity<List<Room>> getRoomsWithWumpus() {
        log.info("Getting all rooms with Wumpus");
        List<Room> rooms = roomService.getRoomsWithWumpus();
        log.info("Found {} rooms with Wumpus", rooms.size());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get all rooms with pits.
     *
     * @return List of rooms with pits
     */
    @GetMapping("/pits")
    public ResponseEntity<List<Room>> getRoomsWithPits() {
        log.info("Getting all rooms with pits");
        List<Room> rooms = roomService.getRoomsWithPits();
        log.info("Found {} rooms with pits", rooms.size());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get all rooms with bats.
     *
     * @return List of rooms with bats
     */
    @GetMapping("/bats")
    public ResponseEntity<List<Room>> getRoomsWithBats() {
        log.info("Getting all rooms with bats");
        List<Room> rooms = roomService.getRoomsWithBats();
        log.info("Found {} rooms with bats", rooms.size());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Request class for placing hazards.
     */
    public static class PlaceHazardsRequest {
        private int wumpusCount;
        private int pitCount;
        private int batCount;

        public int getWumpusCount() {
            return wumpusCount;
        }

        public void setWumpusCount(int wumpusCount) {
            this.wumpusCount = wumpusCount;
        }

        public int getPitCount() {
            return pitCount;
        }

        public void setPitCount(int pitCount) {
            this.pitCount = pitCount;
        }

        public int getBatCount() {
            return batCount;
        }

        public void setBatCount(int batCount) {
            this.batCount = batCount;
        }
    }
}
