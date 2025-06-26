package org.scarter4work.wumpus2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

/**
 * Represents a room in the cave system.
 */
@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@Slf4j
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    /**
     * ID of the room connected to the north.
     * Null if there is no connection.
     */
    private UUID northRoomId;

    /**
     * ID of the room connected to the east.
     * Null if there is no connection.
     */
    private UUID eastRoomId;

    /**
     * ID of the room connected to the south.
     * Null if there is no connection.
     */
    private UUID southRoomId;

    /**
     * ID of the room connected to the west.
     * Null if there is no connection.
     */
    private UUID westRoomId;

    /**
     * Indicates if this room contains the Wumpus.
     */
    @Column(nullable = false)
    private boolean hasWumpus = false;

    /**
     * Indicates if this room contains a bottomless pit.
     */
    @Column(nullable = false)
    private boolean hasPit = false;

    /**
     * Indicates if this room contains bats.
     */
    @Column(nullable = false)
    private boolean hasBats = false;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    public UUID getNorthRoomId() {
        return northRoomId;
    }

    public void setNorthRoomId(UUID northRoomId) {
        this.northRoomId = northRoomId;
    }

    public UUID getEastRoomId() {
        return eastRoomId;
    }

    public void setEastRoomId(UUID eastRoomId) {
        this.eastRoomId = eastRoomId;
    }

    public UUID getSouthRoomId() {
        return southRoomId;
    }

    public void setSouthRoomId(UUID southRoomId) {
        this.southRoomId = southRoomId;
    }

    public UUID getWestRoomId() {
        return westRoomId;
    }

    public void setWestRoomId(UUID westRoomId) {
        this.westRoomId = westRoomId;
    }

    public boolean isHasWumpus() {
        return hasWumpus;
    }

    public void setHasWumpus(boolean hasWumpus) {
        this.hasWumpus = hasWumpus;
    }

    public boolean isHasPit() {
        return hasPit;
    }

    public void setHasPit(boolean hasPit) {
        this.hasPit = hasPit;
    }

    public boolean isHasBats() {
        return hasBats;
    }

    public void setHasBats(boolean hasBats) {
        this.hasBats = hasBats;
    }

    /**
     * Creates a new room with the specified connections.
     * 
     * @param northRoomId ID of the room to the north
     * @param eastRoomId ID of the room to the east
     * @param southRoomId ID of the room to the south
     * @param westRoomId ID of the room to the west
     * @return A new Room instance
     */
    public static Room createRoom(UUID northRoomId, UUID eastRoomId, 
                                 UUID southRoomId, UUID westRoomId) {
        log.info("Creating new room with connections - North: {}, East: {}, South: {}, West: {}", 
                northRoomId, eastRoomId, southRoomId, westRoomId);
        Room room = new Room();
        room.setNorthRoomId(northRoomId);
        room.setEastRoomId(eastRoomId);
        room.setSouthRoomId(southRoomId);
        room.setWestRoomId(westRoomId);
        log.info("New room created");
        return room;
    }
}
