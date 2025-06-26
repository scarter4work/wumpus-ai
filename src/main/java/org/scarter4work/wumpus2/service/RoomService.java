package org.scarter4work.wumpus2.service;

import org.scarter4work.wumpus2.model.Game;
import org.scarter4work.wumpus2.model.GameRoom;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.repository.GameRepository;
import org.scarter4work.wumpus2.repository.GameRoomRepository;
import org.scarter4work.wumpus2.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service class for managing room operations in Hunt the Wumpus.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final GameRoomRepository gameRoomRepository;
    private final GameRepository gameRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository, GameRoomRepository gameRoomRepository, GameRepository gameRepository) {
        this.roomRepository = roomRepository;
        this.gameRoomRepository = gameRoomRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Creates a new room with the specified connections.
     *
     * @param northRoomId ID of the room to the north
     * @param eastRoomId ID of the room to the east
     * @param southRoomId ID of the room to the south
     * @param westRoomId ID of the room to the west
     * @return The newly created room
     */
    @Transactional
    public Room createRoom(UUID northRoomId, UUID eastRoomId, UUID southRoomId, UUID westRoomId) {
        Room room = Room.createRoom(northRoomId, eastRoomId, southRoomId, westRoomId);
        return roomRepository.save(room);
    }

    /**
     * Retrieves a room by its ID.
     *
     * @param roomId The ID of the room to retrieve
     * @return The room if found, otherwise null
     */
    public Room getRoom(UUID roomId) {
        return roomRepository.findById(roomId).orElse(null);
    }

    /**
     * Gets all rooms in the system.
     *
     * @return List of all rooms
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Gets all rooms associated with a specific game.
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
     * Gets all rooms that contain the Wumpus.
     *
     * @return List of rooms containing the Wumpus
     */
    public List<Room> getRoomsWithWumpus() {
        return roomRepository.findByHasWumpusTrue();
    }

    /**
     * Gets all rooms that contain pits.
     *
     * @return List of rooms containing pits
     */
    public List<Room> getRoomsWithPits() {
        return roomRepository.findByHasPitTrue();
    }

    /**
     * Gets all rooms that contain bats.
     *
     * @return List of rooms containing bats
     */
    public List<Room> getRoomsWithBats() {
        return roomRepository.findByHasBatsTrue();
    }

    /**
     * Gets all rooms connected to the specified room.
     *
     * @param roomId The ID of the room
     * @return List of connected rooms
     */
    public List<Room> getConnectedRooms(UUID roomId) {
        return roomRepository.findByNorthRoomIdOrEastRoomIdOrSouthRoomIdOrWestRoomId(
                roomId, roomId, roomId, roomId);
    }

    /**
     * Gets the adjacent rooms to the specified room.
     *
     * @param roomId The ID of the room
     * @return Map of directions to adjacent rooms
     */
    public Map<String, Room> getAdjacentRooms(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        Map<String, Room> adjacentRooms = new HashMap<>();
        
        if (room.getNorthRoomId() != null) {
            roomRepository.findById(room.getNorthRoomId())
                    .ifPresent(northRoom -> adjacentRooms.put("north", northRoom));
        }
        
        if (room.getEastRoomId() != null) {
            roomRepository.findById(room.getEastRoomId())
                    .ifPresent(eastRoom -> adjacentRooms.put("east", eastRoom));
        }
        
        if (room.getSouthRoomId() != null) {
            roomRepository.findById(room.getSouthRoomId())
                    .ifPresent(southRoom -> adjacentRooms.put("south", southRoom));
        }
        
        if (room.getWestRoomId() != null) {
            roomRepository.findById(room.getWestRoomId())
                    .ifPresent(westRoom -> adjacentRooms.put("west", westRoom));
        }
        
        return adjacentRooms;
    }

    /**
     * Places hazards in the specified rooms for a game.
     *
     * @param gameId The ID of the game
     * @param wumpusRoomId The ID of the room to place the Wumpus
     * @param pitRoomIds List of room IDs to place pits
     * @param batRoomIds List of room IDs to place bats
     * @return True if hazards were placed successfully
     */
    @Transactional
    public boolean placeHazards(UUID gameId, UUID wumpusRoomId, List<UUID> pitRoomIds, List<UUID> batRoomIds) {
        // Verify the game exists
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Get all rooms for the game
        List<Room> gameRooms = getRoomsForGame(gameId);
        Set<UUID> gameRoomIds = new HashSet<>();
        for (Room room : gameRooms) {
            gameRoomIds.add(room.getId());
        }
        
        // Verify all specified rooms are part of the game
        if (!gameRoomIds.contains(wumpusRoomId)) {
            throw new IllegalArgumentException("Wumpus room is not part of the game");
        }
        
        for (UUID pitRoomId : pitRoomIds) {
            if (!gameRoomIds.contains(pitRoomId)) {
                throw new IllegalArgumentException("Pit room is not part of the game");
            }
        }
        
        for (UUID batRoomId : batRoomIds) {
            if (!gameRoomIds.contains(batRoomId)) {
                throw new IllegalArgumentException("Bat room is not part of the game");
            }
        }
        
        // Place the Wumpus
        Room wumpusRoom = roomRepository.findById(wumpusRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Wumpus room not found"));
        wumpusRoom.setHasWumpus(true);
        roomRepository.save(wumpusRoom);
        
        // Place pits
        for (UUID pitRoomId : pitRoomIds) {
            Room pitRoom = roomRepository.findById(pitRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("Pit room not found"));
            pitRoom.setHasPit(true);
            roomRepository.save(pitRoom);
        }
        
        // Place bats
        for (UUID batRoomId : batRoomIds) {
            Room batRoom = roomRepository.findById(batRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("Bat room not found"));
            batRoom.setHasBats(true);
            roomRepository.save(batRoom);
        }
        
        return true;
    }

    /**
     * Places hazards randomly in rooms for a game.
     *
     * @param gameId The ID of the game
     * @return True if hazards were placed successfully
     */
    @Transactional
    public boolean placeRandomHazards(UUID gameId) {
        // Get all rooms for the game
        List<Room> rooms = getRoomsForGame(gameId);

        if (rooms.isEmpty()) {
            throw new IllegalStateException("No rooms found for the game");
        }

        // Validate we have enough rooms for all hazards (1 wumpus + 3 pits + 3 bats = 7 minimum)
        if (rooms.size() < 7) {
            throw new IllegalStateException("Not enough rooms to place all hazards");
        }

        // Clear any existing hazards in bulk
        rooms.forEach(room -> {
            room.setHasWumpus(false);
            room.setHasPit(false);
            room.setHasBats(false);
        });

        // Create shuffled list of room indices to avoid conflicts
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            availableIndices.add(i);
        }
        Collections.shuffle(availableIndices);

        int indexCounter = 0;

        // Place 1 Wumpus
        Room wumpusRoom = rooms.get(availableIndices.get(indexCounter++));
        wumpusRoom.setHasWumpus(true);

        // Place 3 pits
        for (int i = 0; i < 3; i++) {
            Room pitRoom = rooms.get(availableIndices.get(indexCounter++));
            pitRoom.setHasPit(true);
        }

        // Place 3 bat colonies
        for (int i = 0; i < 3; i++) {
            Room batRoom = rooms.get(availableIndices.get(indexCounter++));
            batRoom.setHasBats(true);
        }

        // Save all rooms in a single batch operation
        roomRepository.saveAll(rooms);

        return true;
    }

    /**
     * Checks if a room has any hazards.
     *
     * @param roomId The ID of the room to check
     * @return True if the room has any hazards
     */
    public boolean hasHazards(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        return room.isHasWumpus() || room.isHasPit() || room.isHasBats();
    }

    /**
     * Gets information about hazards in adjacent rooms.
     *
     * @param roomId The ID of the room
     * @return Map of hazard types to lists of directions
     */
    public Map<String, List<String>> getAdjacentHazardInfo(UUID roomId) {
        Map<String, Room> adjacentRooms = getAdjacentRooms(roomId);
        Map<String, List<String>> hazardInfo = new HashMap<>();
        
        hazardInfo.put("wumpus", new ArrayList<>());
        hazardInfo.put("pit", new ArrayList<>());
        hazardInfo.put("bats", new ArrayList<>());
        
        for (Map.Entry<String, Room> entry : adjacentRooms.entrySet()) {
            String direction = entry.getKey();
            Room room = entry.getValue();
            
            if (room.isHasWumpus()) {
                hazardInfo.get("wumpus").add(direction);
            }
            
            if (room.isHasPit()) {
                hazardInfo.get("pit").add(direction);
            }
            
            if (room.isHasBats()) {
                hazardInfo.get("bats").add(direction);
            }
        }
        
        return hazardInfo;
    }

    /**
     * Gets a description of hazards near the player.
     *
     * @param gameId The ID of the game
     * @return List of hazard descriptions
     */
    public List<String> getHazardDescriptions(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        UUID currentRoomId = game.getCurrentRoomId();
        Map<String, List<String>> hazardInfo = getAdjacentHazardInfo(currentRoomId);
        
        List<String> descriptions = new ArrayList<>();
        
        if (!hazardInfo.get("wumpus").isEmpty()) {
            descriptions.add("You smell a wumpus nearby!");
        }
        
        if (!hazardInfo.get("pit").isEmpty()) {
            descriptions.add("You feel a draft from a nearby pit!");
        }
        
        if (!hazardInfo.get("bats").isEmpty()) {
            descriptions.add("You hear bats rustling nearby!");
        }
        
        return descriptions;
    }

    /**
     * Creates a new cave system with interconnected rooms.
     *
     * @param numRooms The number of rooms to create
     * @return List of created rooms
     */
    @Transactional
    public List<Room> createCaveSystem(int numRooms) {
        if (numRooms < 5) {
            throw new IllegalArgumentException("Cave system must have at least 5 rooms");
        }
        
        // Create rooms
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < numRooms; i++) {
            Room room = new Room();
            rooms.add(roomRepository.save(room));
        }
        
        // Connect the rooms in a circular pattern with additional connections
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            
            // Basic circular connections
            room.setNorthRoomId(rooms.get((i + 1) % rooms.size()).getId());
            room.setSouthRoomId(rooms.get((i - 1 + rooms.size()) % rooms.size()).getId());
            
            // Additional connections to make the cave more complex
            room.setEastRoomId(rooms.get((i + 3) % rooms.size()).getId());
            
            // Not all rooms have a west connection
            if (i % 2 == 0) {
                room.setWestRoomId(rooms.get((i + numRooms / 2) % rooms.size()).getId());
            }
            
            roomRepository.save(room);
        }
        
        return rooms;
    }

    /**
     * Represents information about hazards in the game.
     */
    public static class HazardInfo {
        private final boolean hasWumpus;
        private final boolean hasPit;
        private final boolean hasBats;
        private final List<String> descriptions;

        public HazardInfo(boolean hasWumpus, boolean hasPit, boolean hasBats, List<String> descriptions) {
            this.hasWumpus = hasWumpus;
            this.hasPit = hasPit;
            this.hasBats = hasBats;
            this.descriptions = descriptions;
        }

        public boolean isHasWumpus() {
            return hasWumpus;
        }

        public boolean isHasPit() {
            return hasPit;
        }

        public boolean isHasBats() {
            return hasBats;
        }

        public List<String> getDescriptions() {
            return descriptions;
        }
    }
}