package org.scarter4work.wumpus2.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.scarter4work.wumpus2.model.Game;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.service.GameService;
import org.scarter4work.wumpus2.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private GameController gameController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createGame() {
        // Arrange
        GameController.CreateGameRequest request = new GameController.CreateGameRequest();
        request.setPlayerName("TestPlayer");

        Game mockGame = new Game();
        mockGame.setId(UUID.randomUUID());
        mockGame.setPlayerName("TestPlayer");

        when(gameService.createNewGame(anyString())).thenReturn(mockGame);

        // Act
        ResponseEntity<Game> response = gameController.createGame(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TestPlayer", response.getBody().getPlayerName());
        verify(gameService, times(1)).createNewGame("TestPlayer");
    }

    @Test
    void getGameState() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        
        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setPlayerName("TestPlayer");
        
        Room mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setRoomNumber(1);
        
        Map<String, Boolean> mockHazardInfo = new HashMap<>();
        mockHazardInfo.put("wumpus", false);
        mockHazardInfo.put("pit", false);
        mockHazardInfo.put("bats", false);
        
        Set<UUID> mockVisitedRooms = new HashSet<>();
        mockVisitedRooms.add(mockRoom.getId());

        when(gameService.getGame(gameId)).thenReturn(mockGame);
        when(gameService.getCurrentRoom(gameId)).thenReturn(mockRoom);
        when(gameService.getVisitedRooms(gameId)).thenReturn(mockVisitedRooms);
        when(gameService.getHazardInformation(gameId)).thenReturn(mockHazardInfo);
        
        // Act
        ResponseEntity<GameController.GameStateResponse> response = gameController.getGameState(gameId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockGame, response.getBody().getGame());
        assertEquals(mockRoom, response.getBody().getCurrentRoom());
        assertEquals(mockVisitedRooms, response.getBody().getVisitedRooms());
        verify(gameService, times(1)).getGame(gameId);
        verify(gameService, times(1)).getCurrentRoom(gameId);
        verify(gameService, times(1)).getVisitedRooms(gameId);
    }

    @Test
    void movePlayer() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        GameController.MoveRequest request = new GameController.MoveRequest();
        request.setDirection("north");
        
        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setPlayerName("TestPlayer");
        
        Room mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setRoomNumber(1);
        
        Map<String, Boolean> mockHazardInfo = new HashMap<>();
        mockHazardInfo.put("wumpus", false);
        mockHazardInfo.put("pit", false);
        mockHazardInfo.put("bats", false);
        
        Set<UUID> mockVisitedRooms = new HashSet<>();
        mockVisitedRooms.add(mockRoom.getId());

        when(gameService.movePlayer(gameId, "north")).thenReturn(mockGame);
        when(gameService.getCurrentRoom(gameId)).thenReturn(mockRoom);
        when(gameService.getVisitedRooms(gameId)).thenReturn(mockVisitedRooms);
        when(gameService.getHazardInformation(gameId)).thenReturn(mockHazardInfo);
        
        // Act
        ResponseEntity<GameController.GameStateResponse> response = gameController.movePlayer(gameId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockGame, response.getBody().getGame());
        assertEquals(mockRoom, response.getBody().getCurrentRoom());
        assertEquals(mockVisitedRooms, response.getBody().getVisitedRooms());
        verify(gameService, times(1)).movePlayer(gameId, "north");
        verify(gameService, times(1)).getCurrentRoom(gameId);
        verify(gameService, times(1)).getVisitedRooms(gameId);
    }

    @Test
    void shootArrow() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        GameController.ShootRequest request = new GameController.ShootRequest();
        request.setDirection("north");
        
        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setPlayerName("TestPlayer");
        
        Room mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setRoomNumber(1);
        
        Map<String, Boolean> mockHazardInfo = new HashMap<>();
        mockHazardInfo.put("wumpus", false);
        mockHazardInfo.put("pit", false);
        mockHazardInfo.put("bats", false);
        
        Set<UUID> mockVisitedRooms = new HashSet<>();
        mockVisitedRooms.add(mockRoom.getId());

        when(gameService.shootArrow(gameId, "north")).thenReturn(mockGame);
        when(gameService.getCurrentRoom(gameId)).thenReturn(mockRoom);
        when(gameService.getVisitedRooms(gameId)).thenReturn(mockVisitedRooms);
        when(gameService.getHazardInformation(gameId)).thenReturn(mockHazardInfo);
        
        // Act
        ResponseEntity<GameController.GameStateResponse> response = gameController.shootArrow(gameId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockGame, response.getBody().getGame());
        assertEquals(mockRoom, response.getBody().getCurrentRoom());
        assertEquals(mockVisitedRooms, response.getBody().getVisitedRooms());
        verify(gameService, times(1)).shootArrow(gameId, "north");
        verify(gameService, times(1)).getCurrentRoom(gameId);
        verify(gameService, times(1)).getVisitedRooms(gameId);
    }
}