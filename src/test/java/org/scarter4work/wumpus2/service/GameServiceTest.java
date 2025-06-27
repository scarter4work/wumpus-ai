package org.scarter4work.wumpus2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.scarter4work.wumpus2.model.Game;
import org.scarter4work.wumpus2.model.GameRoom;
import org.scarter4work.wumpus2.model.GameVisitedRoom;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.repository.GameRepository;
import org.scarter4work.wumpus2.repository.GameRoomRepository;
import org.scarter4work.wumpus2.repository.GameVisitedRoomRepository;
import org.scarter4work.wumpus2.repository.RoomRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private GameVisitedRoomRepository gameVisitedRoomRepository;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createNewGame() {
        // Arrange
        String playerName = "TestPlayer";

        // Mock room creation
        List<Room> mockRooms = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            Room room = new Room();
            room.setId(UUID.randomUUID());
            room.setRoomNumber(i);
            mockRooms.add(room);
        }

        // Mock game creation
        Game mockGame = new Game();
        mockGame.setId(UUID.randomUUID());
        mockGame.setPlayerName(playerName);

        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            if (room.getId() == null) {
                room.setId(UUID.randomUUID());
            }
            return room;
        });

        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            if (game.getId() == null) {
                game.setId(UUID.randomUUID());
            }
            return game;
        });

        when(gameRoomRepository.save(any(GameRoom.class))).thenAnswer(invocation -> {
            GameRoom gameRoom = invocation.getArgument(0);
            if (gameRoom.getId() == null) {
                gameRoom.setId(UUID.randomUUID());
            }
            return gameRoom;
        });

        // Act
        Game result = gameService.createNewGame(playerName);

        // Assert
        assertNotNull(result);
        assertEquals(playerName, result.getPlayerName());
        verify(gameRepository, atLeast(1)).save(any(Game.class));
        verify(roomRepository, atLeast(25)).save(any(Room.class));
        verify(gameRoomRepository, atLeast(25)).save(any(GameRoom.class));
    }

    @Test
    void getGame() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setPlayerName("TestPlayer");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));

        // Act
        Game result = gameService.getGame(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(gameId, result.getId());
        assertEquals("TestPlayer", result.getPlayerName());
        verify(gameRepository, times(1)).findById(gameId);
    }

    @Test
    void getGamesByPlayer() {
        // Arrange
        String playerName = "TestPlayer";
        List<Game> mockGames = new ArrayList<>();

        Game game1 = new Game();
        game1.setId(UUID.randomUUID());
        game1.setPlayerName(playerName);
        mockGames.add(game1);

        Game game2 = new Game();
        game2.setId(UUID.randomUUID());
        game2.setPlayerName(playerName);
        mockGames.add(game2);

        when(gameRepository.findByPlayerName(playerName)).thenReturn(mockGames);

        // Act
        List<Game> result = gameService.getGamesByPlayer(playerName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(playerName, result.get(0).getPlayerName());
        assertEquals(playerName, result.get(1).getPlayerName());
        verify(gameRepository, times(1)).findByPlayerName(playerName);
    }

    @Test
    void movePlayer() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        String direction = "north";

        Room currentRoom = new Room();
        currentRoom.setId(UUID.randomUUID());
        currentRoom.setRoomNumber(1);

        Room northRoom = new Room();
        northRoom.setId(UUID.randomUUID());
        northRoom.setRoomNumber(2);
        currentRoom.setNorthRoomId(northRoom.getId());

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setPlayerName("TestPlayer");
        mockGame.setStatus(Game.GameStatus.IN_PROGRESS);
        mockGame.setCurrentRoomId(currentRoom.getId());

        // Mock the updated game that will be returned after moving
        Game updatedGame = new Game();
        updatedGame.setId(gameId);
        updatedGame.setPlayerName("TestPlayer");
        updatedGame.setStatus(Game.GameStatus.IN_PROGRESS);
        updatedGame.setCurrentRoomId(northRoom.getId());

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roomRepository.findById(currentRoom.getId())).thenReturn(Optional.of(currentRoom));
        when(roomRepository.findById(northRoom.getId())).thenReturn(Optional.of(northRoom));
        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        // Act
        Game result = gameService.movePlayer(gameId, direction);

        // Assert
        assertNotNull(result);
        assertEquals(gameId, result.getId());
        assertEquals(northRoom.getId(), result.getCurrentRoomId());
        verify(gameRepository, times(1)).findById(gameId);
        verify(roomRepository, atLeastOnce()).findById(currentRoom.getId());
        verify(roomRepository, atLeastOnce()).findById(northRoom.getId());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void getCurrentRoom() {
        // Arrange
        UUID gameId = UUID.randomUUID();

        Room mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setRoomNumber(1);

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setCurrentRoomId(mockRoom.getId());

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));

        // Act
        Room result = gameService.getCurrentRoom(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(mockRoom.getId(), result.getId());
        verify(gameRepository, times(1)).findById(gameId);
        verify(roomRepository, times(1)).findById(mockRoom.getId());
    }

    @Test
    void getVisitedRooms() {
        // Arrange
        UUID gameId = UUID.randomUUID();

        List<GameVisitedRoom> mockVisitedRooms = new ArrayList<>();

        UUID room1Id = UUID.randomUUID();
        GameVisitedRoom visitedRoom1 = new GameVisitedRoom();
        visitedRoom1.setGameId(gameId);
        visitedRoom1.setRoomId(room1Id);
        mockVisitedRooms.add(visitedRoom1);

        UUID room2Id = UUID.randomUUID();
        GameVisitedRoom visitedRoom2 = new GameVisitedRoom();
        visitedRoom2.setGameId(gameId);
        visitedRoom2.setRoomId(room2Id);
        mockVisitedRooms.add(visitedRoom2);

        when(gameVisitedRoomRepository.findByGameId(gameId)).thenReturn(mockVisitedRooms);

        // Act
        Set<UUID> result = gameService.getVisitedRooms(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(room1Id));
        assertTrue(result.contains(room2Id));
        verify(gameVisitedRoomRepository, times(1)).findByGameId(gameId);
    }
}
