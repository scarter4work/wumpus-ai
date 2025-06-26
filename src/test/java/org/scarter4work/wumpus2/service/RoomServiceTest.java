package org.scarter4work.wumpus2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.scarter4work.wumpus2.model.Game;
import org.scarter4work.wumpus2.model.GameRoom;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.repository.GameRepository;
import org.scarter4work.wumpus2.repository.GameRoomRepository;
import org.scarter4work.wumpus2.repository.RoomRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRoom() {
        // Arrange
        UUID northRoomId = UUID.randomUUID();
        UUID eastRoomId = UUID.randomUUID();
        UUID southRoomId = UUID.randomUUID();
        UUID westRoomId = UUID.randomUUID();
        
        Room mockRoom = new Room();
        mockRoom.setId(UUID.randomUUID());
        mockRoom.setNorthRoomId(northRoomId);
        mockRoom.setEastRoomId(eastRoomId);
        mockRoom.setSouthRoomId(southRoomId);
        mockRoom.setWestRoomId(westRoomId);
        
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);

        // Act
        Room result = roomService.createRoom(northRoomId, eastRoomId, southRoomId, westRoomId);

        // Assert
        assertNotNull(result);
        assertEquals(northRoomId, result.getNorthRoomId());
        assertEquals(eastRoomId, result.getEastRoomId());
        assertEquals(southRoomId, result.getSouthRoomId());
        assertEquals(westRoomId, result.getWestRoomId());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void getRoom() {
        // Arrange
        UUID roomId = UUID.randomUUID();
        Room mockRoom = new Room();
        mockRoom.setId(roomId);
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));

        // Act
        Room result = roomService.getRoom(roomId);

        // Assert
        assertNotNull(result);
        assertEquals(roomId, result.getId());
        verify(roomRepository, times(1)).findById(roomId);
    }

    @Test
    void getAllRooms() {
        // Arrange
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        mockRooms.add(room1);
        
        Room room2 = new Room();
        room2.setId(UUID.randomUUID());
        mockRooms.add(room2);
        
        when(roomRepository.findAll()).thenReturn(mockRooms);

        // Act
        List<Room> result = roomService.getAllRooms();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void getRoomsForGame() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        
        List<GameRoom> mockGameRooms = new ArrayList<>();
        
        UUID room1Id = UUID.randomUUID();
        GameRoom gameRoom1 = new GameRoom();
        gameRoom1.setGameId(gameId);
        gameRoom1.setRoomId(room1Id);
        mockGameRooms.add(gameRoom1);
        
        UUID room2Id = UUID.randomUUID();
        GameRoom gameRoom2 = new GameRoom();
        gameRoom2.setGameId(gameId);
        gameRoom2.setRoomId(room2Id);
        mockGameRooms.add(gameRoom2);
        
        Room mockRoom1 = new Room();
        mockRoom1.setId(room1Id);
        
        Room mockRoom2 = new Room();
        mockRoom2.setId(room2Id);
        
        when(gameRoomRepository.findByGameId(gameId)).thenReturn(mockGameRooms);
        when(roomRepository.findById(room1Id)).thenReturn(Optional.of(mockRoom1));
        when(roomRepository.findById(room2Id)).thenReturn(Optional.of(mockRoom2));

        // Act
        List<Room> result = roomService.getRoomsForGame(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gameRoomRepository, times(1)).findByGameId(gameId);
        verify(roomRepository, times(1)).findById(room1Id);
        verify(roomRepository, times(1)).findById(room2Id);
    }

    @Test
    void getRoomsWithWumpus() {
        // Arrange
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        room1.setHasWumpus(true);
        mockRooms.add(room1);
        
        when(roomRepository.findByHasWumpusTrue()).thenReturn(mockRooms);

        // Act
        List<Room> result = roomService.getRoomsWithWumpus();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isHasWumpus());
        verify(roomRepository, times(1)).findByHasWumpusTrue();
    }

    @Test
    void getRoomsWithPits() {
        // Arrange
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        room1.setHasPit(true);
        mockRooms.add(room1);
        
        Room room2 = new Room();
        room2.setId(UUID.randomUUID());
        room2.setHasPit(true);
        mockRooms.add(room2);
        
        when(roomRepository.findByHasPitTrue()).thenReturn(mockRooms);

        // Act
        List<Room> result = roomService.getRoomsWithPits();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isHasPit());
        assertTrue(result.get(1).isHasPit());
        verify(roomRepository, times(1)).findByHasPitTrue();
    }

    @Test
    void getRoomsWithBats() {
        // Arrange
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        room1.setHasBats(true);
        mockRooms.add(room1);
        
        when(roomRepository.findByHasBatsTrue()).thenReturn(mockRooms);

        // Act
        List<Room> result = roomService.getRoomsWithBats();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isHasBats());
        verify(roomRepository, times(1)).findByHasBatsTrue();
    }

    @Test
    void createCaveSystem() {
        // Arrange
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            if (room.getId() == null) {
                room.setId(UUID.randomUUID());
            }
            return room;
        });

        // Act
        List<Room> result = roomService.createCaveSystem(10);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.size());
        verify(roomRepository, times(20)).save(any(Room.class)); // Initial save + connections save
    }
}