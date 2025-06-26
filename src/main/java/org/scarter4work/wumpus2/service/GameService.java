package org.scarter4work.wumpus2.service;

import lombok.extern.slf4j.Slf4j;
import org.scarter4work.wumpus2.model.Game;
import org.scarter4work.wumpus2.model.GameRoom;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.repository.GameRepository;
import org.scarter4work.wumpus2.repository.GameRoomRepository;
import org.scarter4work.wumpus2.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import org.scarter4work.wumpus2.model.GameVisitedRoom;
import org.scarter4work.wumpus2.repository.GameVisitedRoomRepository;
import java.util.stream.Collectors;

/**
 * Service class for managing Hunt the Wumpus game operations.
 */
@Service
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final RoomRepository roomRepository;
    private final GameRoomRepository gameRoomRepository;
    private final GameVisitedRoomRepository gameVisitedRoomRepository;
    @Autowired
    public GameService(GameRepository gameRepository, RoomRepository roomRepository,
                       GameRoomRepository gameRoomRepository, GameVisitedRoomRepository gameVisitedRoomRepository) {
        this.gameRepository = gameRepository;
        this.roomRepository = roomRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.gameVisitedRoomRepository = gameVisitedRoomRepository;
    }

    /**
     * Creates a new game with a randomly generated cave system.
     *
     * @param playerName The name of the player
     * @return The newly created game
     */
    @Transactional
    public Game createNewGame(String playerName) {
        // Create a set of rooms for the cave system
        List<Room> rooms = createCaveSystem();

        // Choose a random room as the starting point (that doesn't have hazards)
        Room startingRoom = findSafeStartingRoom(rooms);

        // Create the game
        Game game = Game.createNewGame(playerName, startingRoom.getId());

        // Save the game first to get the generated ID
        game = gameRepository.save(game);

        // Now mark the starting room as visited (after game has an ID)
        markRoomAsVisited(game.getId(), startingRoom.getId());

        // Associate all rooms with the game
        for (Room room : rooms) {
            GameRoom gameRoom = GameRoom.createGameRoom(game.getId(), room.getId());
            gameRoomRepository.save(gameRoom);
        }

        return game;
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param gameId The ID of the game to retrieve
     * @return The game if found, otherwise null
     */
    public Game getGame(UUID gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    /**
     * Retrieves all games for a player.
     *
     * @param playerName The name of the player
     * @return List of games for the player
     */
    public List<Game> getGamesByPlayer(String playerName) {
        return gameRepository.findByPlayerName(playerName);
    }

    /**
     * Retrieves all in-progress games for a player.
     *
     * @param playerName The name of the player
     * @return List of in-progress games for the player
     */
    public List<Game> getInProgressGamesByPlayer(String playerName) {
        return gameRepository.findByPlayerNameAndStatus(playerName, Game.GameStatus.IN_PROGRESS);
    }

    /**
     * Moves the player to an adjacent room.
     *
     * @param gameId The ID of the game
     * @param direction The direction to move (north, east, south, west)
     * @return The updated game state
     * @throws IllegalArgumentException if the move is invalid
     */
    @Transactional
    public Game movePlayer(UUID gameId, String direction) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        Room currentRoom = roomRepository.findById(game.getCurrentRoomId())
                .orElseThrow(() -> new IllegalStateException("Current room not found"));

        UUID nextRoomId = switch (direction.toLowerCase()) {
            case "north" -> currentRoom.getNorthRoomId();
            case "east" -> currentRoom.getEastRoomId();
            case "south" -> currentRoom.getSouthRoomId();
            case "west" -> currentRoom.getWestRoomId();
            default -> throw new IllegalArgumentException("Invalid direction");
        };

        log.info("Moving player {} to room {}", direction.toLowerCase(), nextRoomId);

        // Determine the next room based on the direction
        if (nextRoomId == null) {
            throw new IllegalArgumentException("Cannot move in that direction");
        }

        Room nextRoom = roomRepository.findById(nextRoomId)
                .orElseThrow(() -> new IllegalStateException("Next room not found"));

        // Update the player's current room
        game.setCurrentRoomId(nextRoom.getId());

        // Check for hazards in the new room
        checkForHazards(game, nextRoom);

        // Mark the new room as visited
        markRoomAsVisited(gameId, game.getCurrentRoomId());

        return gameRepository.save(game);
    }

    /**
     * Shoots an arrow in the specified direction.
     *
     * @param gameId The ID of the game
     * @param direction The direction to shoot (north, east, south, west)
     * @return The updated game state
     * @throws IllegalArgumentException if the shot is invalid
     */
    @Transactional
    public Game shootArrow(UUID gameId, String direction) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        if (game.getArrowsRemaining() <= 0) {
            throw new IllegalStateException("No arrows remaining");
        }

        Room currentRoom = roomRepository.findById(game.getCurrentRoomId())
                .orElseThrow(() -> new IllegalStateException("Current room not found"));

        UUID targetRoomId = switch (direction.toLowerCase()) {
            case "north" -> currentRoom.getNorthRoomId();
            case "east" -> currentRoom.getEastRoomId();
            case "south" -> currentRoom.getSouthRoomId();
            case "west" -> currentRoom.getWestRoomId();
            default -> throw new IllegalArgumentException("Invalid direction");
        };

        // Determine the target room based on the direction
        if (targetRoomId == null) {
            throw new IllegalArgumentException("Cannot shoot in that direction");
        }

        // Decrease arrow count
        game.setArrowsRemaining(game.getArrowsRemaining() - 1);

        // Check if the arrow hit the Wumpus
        Room targetRoom = roomRepository.findById(targetRoomId)
                .orElseThrow(() -> new IllegalStateException("Target room not found"));

        if (targetRoom.isHasWumpus()) {
            // Wumpus is killed, player wins
            game.setStatus(Game.GameStatus.WON);
            game.setEndTime(LocalDateTime.now());
        } else if (game.getArrowsRemaining() == 0) {
            // Out of arrows, player loses
            game.setStatus(Game.GameStatus.LOST);
            game.setEndTime(LocalDateTime.now());
        }

        return gameRepository.save(game);
    }

    /**
     * Ends a game, setting its status to LOST.
     *
     * @param gameId The ID of the game to end
     * @return The updated game
     */
    @Transactional
    public Game endGame(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getStatus() == Game.GameStatus.IN_PROGRESS) {
            game.setStatus(Game.GameStatus.LOST);
            game.setEndTime(LocalDateTime.now());
            return gameRepository.save(game);
        }

        return game;
    }

    /**
     * Gets all rooms associated with a game.
     *
     * @param gameId The ID of the game
     * @return List of rooms in the game
     */
    public List<Room> getRoomsForGame(UUID gameId) {
        List<GameRoom> gameRooms = gameRoomRepository.findByGameId(gameId);
        List<Room> rooms = new ArrayList<>();

        for (GameRoom gameRoom : gameRooms) {
            roomRepository.findById(gameRoom.getRoomId()).ifPresent(rooms::add);
        }

        return rooms;
    }

    /**
     * Gets the current room for a game.
     *
     * @param gameId The ID of the game
     * @return The current room
     */
    public Room getCurrentRoom(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return roomRepository.findById(game.getCurrentRoomId())
                .orElseThrow(() -> new IllegalStateException("Current room not found"));
    }

    /**
     * Gets adjacent rooms to the player's current room.
     *
     * @param gameId The ID of the game
     * @return Map of directions to room IDs
     */
    public Map<String, UUID> getAdjacentRooms(UUID gameId) {
        Room currentRoom = getCurrentRoom(gameId);
        Map<String, UUID> adjacentRooms = new HashMap<>();

        if (currentRoom.getNorthRoomId() != null) {
            adjacentRooms.put("north", currentRoom.getNorthRoomId());
        }

        if (currentRoom.getEastRoomId() != null) {
            adjacentRooms.put("east", currentRoom.getEastRoomId());
        }

        if (currentRoom.getSouthRoomId() != null) {
            adjacentRooms.put("south", currentRoom.getSouthRoomId());
        }

        if (currentRoom.getWestRoomId() != null) {
            adjacentRooms.put("west", currentRoom.getWestRoomId());
        }

        return adjacentRooms;
    }

    /**
     * Checks for hazards in a room and updates the game state accordingly.
     *
     * @param game The game
     * @param room The room to check for hazards
     */
    private void checkForHazards(Game game, Room room) {
        if (room.isHasWumpus()) {
            // Player encountered the Wumpus and lost
            game.setStatus(Game.GameStatus.LOST);
            game.setEndTime(LocalDateTime.now());
        } else if (room.isHasPit()) {
            // Player fell into a pit and lost
            game.setStatus(Game.GameStatus.LOST);
            game.setEndTime(LocalDateTime.now());
        } else if (room.isHasBats()) {
            // Super bats transport the player to a random room
            List<Room> allRooms = getRoomsForGame(game.getId());
            if (!allRooms.isEmpty()) {
                int randomIndex = new Random().nextInt(allRooms.size());
                Room randomRoom = allRooms.get(randomIndex);
                game.setCurrentRoomId(randomRoom.getId());

                // Recursively check for hazards in the new room
                checkForHazards(game, randomRoom);
            }
        }
    }

    /**
     * Creates a cave system with interconnected rooms.
     *
     * @return List of rooms in the cave system
     */
    private List<Room> createCaveSystem() {
        // Create 20 rooms for the cave
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Room room = new Room();
            rooms.add(roomRepository.save(room));
        }

        // Connect the rooms in a dodecahedral structure (or simplified version)
        // This is a simplified connection pattern - in a real implementation,
        // you might want a more complex cave structure
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);

            // Connect to 3-4 other rooms
            room.setNorthRoomId(rooms.get((i + 1) % rooms.size()).getId());
            room.setEastRoomId(rooms.get((i + 5) % rooms.size()).getId());
            room.setSouthRoomId(rooms.get((i + 10) % rooms.size()).getId());

            // Not all rooms have a west connection
            if (i % 2 == 0) {
                room.setWestRoomId(rooms.get((i + 15) % rooms.size()).getId());
            }

            roomRepository.save(room);
        }

        // Place hazards
        placeHazards(rooms);

        return rooms;
    }

    /**
     * Places hazards (Wumpus, pits, bats) in the cave system.
     *
     * @param rooms List of rooms in the cave system
     */
    private void placeHazards(List<Room> rooms) {
        Random random = new Random();

        // Place 1 Wumpus
        int wumpusIndex = random.nextInt(rooms.size());
        Room wumpusRoom = rooms.get(wumpusIndex);
        wumpusRoom.setHasWumpus(true);
        roomRepository.save(wumpusRoom);

        // Place 3 pits
        for (int i = 0; i < 3; i++) {
            int pitIndex;
            do {
                pitIndex = random.nextInt(rooms.size());
            } while (rooms.get(pitIndex).isHasWumpus() || rooms.get(pitIndex).isHasPit());

            Room pitRoom = rooms.get(pitIndex);
            pitRoom.setHasPit(true);
            roomRepository.save(pitRoom);
        }

        // Place 3 bat colonies
        for (int i = 0; i < 3; i++) {
            int batIndex;
            do {
                batIndex = random.nextInt(rooms.size());
            } while (rooms.get(batIndex).isHasWumpus() || 
                    rooms.get(batIndex).isHasPit() || 
                    rooms.get(batIndex).isHasBats());

            Room batRoom = rooms.get(batIndex);
            batRoom.setHasBats(true);
            roomRepository.save(batRoom);
        }
    }

    /**
     * Finds a safe room (no hazards) to start the game.
     *
     * @param rooms List of rooms in the cave system
     * @return A safe room
     */
    private Room findSafeStartingRoom(List<Room> rooms) {
        for (Room room : rooms) {
            if (!room.isHasWumpus() && !room.isHasPit() && !room.isHasBats()) {
                return room;
            }
        }

        // If no completely safe room is found (unlikely with 20 rooms and 7 hazards),
        // return the first room without a Wumpus or pit
        for (Room room : rooms) {
            if (!room.isHasWumpus() && !room.isHasPit()) {
                return room;
            }
        }

        // Fallback to the first room
        return rooms.get(0);
    }

    // Add helper methods
    /**
     * Marks a room as visited for a game.
     */
    private void markRoomAsVisited(UUID gameId, UUID roomId) {
        if (!gameVisitedRoomRepository.existsByGameIdAndRoomId(gameId, roomId)) {
            GameVisitedRoom visitedRoom = GameVisitedRoom.createVisitedRoom(gameId, roomId);
            gameVisitedRoomRepository.save(visitedRoom);
        }
    }

    /**
     * Gets all visited room IDs for a game.
     */
    public Set<UUID> getVisitedRooms(UUID gameId) {
        return gameVisitedRoomRepository.findByGameId(gameId)
                .stream()
                .map(GameVisitedRoom::getRoomId)
                .collect(Collectors.toSet());
    }
}
