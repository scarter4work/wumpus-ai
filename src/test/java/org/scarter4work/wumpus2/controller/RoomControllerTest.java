package org.scarter4work.wumpus2.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.scarter4work.wumpus2.model.Room;
import org.scarter4work.wumpus2.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRoomsForGame() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        room1.setRoomNumber(1);
        mockRooms.add(room1);
        
        Room room2 = new Room();
        room2.setId(UUID.randomUUID());
        room2.setRoomNumber(2);
        mockRooms.add(room2);

        when(roomService.getRoomsForGame(gameId)).thenReturn(mockRooms);

        // Act
        ResponseEntity<List<Room>> response = roomController.getRoomsForGame(gameId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(roomService, times(1)).getRoomsForGame(gameId);
    }

    @Test
    void placeHazards() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        RoomController.PlaceHazardsRequest request = new RoomController.PlaceHazardsRequest();
        request.setWumpusCount(1);
        request.setPitCount(2);
        request.setBatCount(2);

        when(roomService.placeRandomHazards(gameId)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = roomController.placeHazards(gameId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
        verify(roomService, times(1)).placeRandomHazards(gameId);
    }

    @Test
    void getRoom() {
        // Arrange
        UUID roomId = UUID.randomUUID();
        Room mockRoom = new Room();
        mockRoom.setId(roomId);
        mockRoom.setRoomNumber(1);

        when(roomService.getRoom(roomId)).thenReturn(mockRoom);

        // Act
        ResponseEntity<Room> response = roomController.getRoom(roomId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(roomId, response.getBody().getId());
        verify(roomService, times(1)).getRoom(roomId);
    }

    @Test
    void getRoomsWithWumpus() {
        // Arrange
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        room1.setRoomNumber(1);
        room1.setHasWumpus(true);
        mockRooms.add(room1);

        when(roomService.getRoomsWithWumpus()).thenReturn(mockRooms);

        // Act
        ResponseEntity<List<Room>> response = roomController.getRoomsWithWumpus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).isHasWumpus());
        verify(roomService, times(1)).getRoomsWithWumpus();
    }

    @Test
    void getRoomsWithPits() {
        // Arrange
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        room1.setRoomNumber(1);
        room1.setHasPit(true);
        mockRooms.add(room1);
        
        Room room2 = new Room();
        room2.setId(UUID.randomUUID());
        room2.setRoomNumber(2);
        room2.setHasPit(true);
        mockRooms.add(room2);

        when(roomService.getRoomsWithPits()).thenReturn(mockRooms);

        // Act
        ResponseEntity<List<Room>> response = roomController.getRoomsWithPits();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).isHasPit());
        assertTrue(response.getBody().get(1).isHasPit());
        verify(roomService, times(1)).getRoomsWithPits();
    }

    @Test
    void getRoomsWithBats() {
        // Arrange
        List<Room> mockRooms = new ArrayList<>();
        
        Room room1 = new Room();
        room1.setId(UUID.randomUUID());
        room1.setRoomNumber(1);
        room1.setHasBats(true);
        mockRooms.add(room1);
        
        Room room2 = new Room();
        room2.setId(UUID.randomUUID());
        room2.setRoomNumber(2);
        room2.setHasBats(true);
        mockRooms.add(room2);

        when(roomService.getRoomsWithBats()).thenReturn(mockRooms);

        // Act
        ResponseEntity<List<Room>> response = roomController.getRoomsWithBats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).isHasBats());
        assertTrue(response.getBody().get(1).isHasBats());
        verify(roomService, times(1)).getRoomsWithBats();
    }
}