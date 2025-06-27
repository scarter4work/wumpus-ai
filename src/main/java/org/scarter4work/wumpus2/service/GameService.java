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

        // Create the game first to get an ID
        Game game = new Game();
        game.setPlayerName(playerName);
        game.setStartTime(LocalDateTime.now());
        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game.setArrowsRemaining(5);
        
        // Temporarily set to first room - will be updated after hazard placement
        game.setCurrentRoomId(rooms.get(0).getId());
        game = gameRepository.save(game);

        // Associate all rooms with the game
        for (Room room : rooms) {
            GameRoom gameRoom = GameRoom.createGameRoom(game.getId(), room.getId());
            gameRoomRepository.save(gameRoom);
        }

        // Place hazards randomly
        placeRandomHazards(rooms);

        // Now choose a safe starting room
        Room startingRoom = findSafeStartingRoom(rooms);
        
        // Update the game with the safe starting room
        game.setCurrentRoomId(startingRoom.getId());
        game = gameRepository.save(game);

        // Mark the starting room as visited
        markRoomAsVisited(game.getId(), startingRoom.getId());

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
        // Create 25 rooms for a 5x5 cave grid
        List<Room> rooms = new ArrayList<>();
        
        // Create rooms with room numbers 1-25
        for (int i = 1; i <= 25; i++) {
            Room room = new Room();
            room.setRoomNumber(i);
            rooms.add(roomRepository.save(room));
        }

        // Connect the rooms in a 5x5 grid structure
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int roomNumber = i + 1; // Room numbers are 1-based
            int row = (roomNumber - 1) / 5; // 0-based row (0-4)
            int col = (roomNumber - 1) % 5; // 0-based column (0-4)

            // Connect to adjacent rooms in the grid
            // North connection (row - 1)
            if (row > 0) {
                int northRoomNumber = roomNumber - 5;
                room.setNorthRoomId(rooms.get(northRoomNumber - 1).getId());
            }

            // South connection (row + 1)
            if (row < 4) {
                int southRoomNumber = roomNumber + 5;
                room.setSouthRoomId(rooms.get(southRoomNumber - 1).getId());
            }

            // East connection (col + 1)
            if (col < 4) {
                int eastRoomNumber = roomNumber + 1;
                room.setEastRoomId(rooms.get(eastRoomNumber - 1).getId());
            }

            // West connection (col - 1)
            if (col > 0) {
                int westRoomNumber = roomNumber - 1;
                room.setWestRoomId(rooms.get(westRoomNumber - 1).getId());
            }

            roomRepository.save(room);
        }

        // Note: Hazards are placed separately via the API call after game creation
        return rooms;
    }

    /**
     * Places hazards (Wumpus, pits, bats) randomly in the cave system.
     *
     * @param rooms List of rooms in the cave system
     */
    private void placeRandomHazards(List<Room> rooms) {
        if (rooms.isEmpty()) {
            throw new IllegalStateException("No rooms found for hazard placement");
        }

        // Validate we have enough rooms for all hazards (1 wumpus + 3 pits + 3 bats = 7 minimum)
        if (rooms.size() < 7) {
            throw new IllegalStateException("Not enough rooms to place all hazards");
        }

        // Clear any existing hazards
        rooms.forEach(room -> {
            room.setHasWumpus(false);
            room.setHasPit(false);
            room.setHasBats(false);
        });

        // Create shuffled list of room indices to ensure random placement without conflicts
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            availableIndices.add(i);
        }
        Collections.shuffle(availableIndices);

        int indexCounter = 0;

        // Place 1 Wumpus
        Room wumpusRoom = rooms.get(availableIndices.get(indexCounter++));
        wumpusRoom.setHasWumpus(true);
        log.info("Placed Wumpus in room {}", wumpusRoom.getRoomNumber());

        // Place 3 pits
        for (int i = 0; i < 3; i++) {
            Room pitRoom = rooms.get(availableIndices.get(indexCounter++));
            pitRoom.setHasPit(true);
            log.info("Placed pit in room {}", pitRoom.getRoomNumber());
        }

        // Place 3 bat colonies
        for (int i = 0; i < 3; i++) {
            Room batRoom = rooms.get(availableIndices.get(indexCounter++));
            batRoom.setHasBats(true);
            log.info("Placed bats in room {}", batRoom.getRoomNumber());
        }

        // Save all rooms in a single batch operation
        roomRepository.saveAll(rooms);
        log.info("Successfully placed all hazards randomly");
    }

    /**
     * Finds a safe room (no hazards) to start the game.
     *
     * @param rooms List of rooms in the cave system
     * @return A safe room
     */
    private Room findSafeStartingRoom(List<Room> rooms) {
        Random random = new Random();
        
        // Find all safe rooms (no hazards)
        List<Room> safeRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (!room.isHasWumpus() && !room.isHasPit() && !room.isHasBats()) {
                safeRooms.add(room);
            }
        }
        
        if (!safeRooms.isEmpty()) {
            // Randomly select from safe rooms
            return safeRooms.get(random.nextInt(safeRooms.size()));
        }

        // If no completely safe room is found (unlikely with 25 rooms and 7 hazards),
        // find rooms without Wumpus or pit and randomly select one
        List<Room> noDeadlyHazards = new ArrayList<>();
        for (Room room : rooms) {
            if (!room.isHasWumpus() && !room.isHasPit()) {
                noDeadlyHazards.add(room);
            }
        }
        
        if (!noDeadlyHazards.isEmpty()) {
            return noDeadlyHazards.get(random.nextInt(noDeadlyHazards.size()));
        }

        // Extremely unlikely fallback - just return a random room
        return rooms.get(random.nextInt(rooms.size()));
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

    /**
     * Gets hazard information for adjacent rooms.
     * 
     * @param gameId The ID of the game
     * @return Map of hazard types to their presence in adjacent rooms
     */
    public Map<String, Boolean> getHazardInformation(UUID gameId) {
        Room currentRoom = getCurrentRoom(gameId);
        Map<String, Boolean> hazardInfo = new HashMap<>();
        
        // Initialize all hazard indicators to false
        hazardInfo.put("wumpusNearby", false);
        hazardInfo.put("pitNearby", false);
        hazardInfo.put("batsNearby", false);
        
        // Check all adjacent rooms for hazards
        List<UUID> adjacentRoomIds = new ArrayList<>();
        if (currentRoom.getNorthRoomId() != null) {
            adjacentRoomIds.add(currentRoom.getNorthRoomId());
        }
        if (currentRoom.getEastRoomId() != null) {
            adjacentRoomIds.add(currentRoom.getEastRoomId());
        }
        if (currentRoom.getSouthRoomId() != null) {
            adjacentRoomIds.add(currentRoom.getSouthRoomId());
        }
        if (currentRoom.getWestRoomId() != null) {
            adjacentRoomIds.add(currentRoom.getWestRoomId());
        }
        
        // Check each adjacent room for hazards
        for (UUID roomId : adjacentRoomIds) {
            Room adjacentRoom = roomRepository.findById(roomId).orElse(null);
            if (adjacentRoom != null) {
                if (adjacentRoom.isHasWumpus()) {
                    hazardInfo.put("wumpusNearby", true);
                }
                if (adjacentRoom.isHasPit()) {
                    hazardInfo.put("pitNearby", true);
                }
                if (adjacentRoom.isHasBats()) {
                    hazardInfo.put("batsNearby", true);
                }
            }
        }
        
        return hazardInfo;
    }
}
