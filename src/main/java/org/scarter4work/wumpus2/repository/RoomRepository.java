package org.scarter4work.wumpus2.repository;

import org.scarter4work.wumpus2.model.Game;
import org.scarter4work.wumpus2.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing and manipulating Room entities.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    /**
     * Find all rooms that contain the Wumpus.
     * 
     * @return List of rooms containing the Wumpus
     */
    List<Room> findByHasWumpusTrue();

    /**
     * Find all rooms that contain a pit.
     * 
     * @return List of rooms containing a pit
     */
    List<Room> findByHasPitTrue();

    /**
     * Find all rooms that contain bats.
     * 
     * @return List of rooms containing bats
     */
    List<Room> findByHasBatsTrue();

    /**
     * Find all rooms connected to the specified room.
     * 
     * @param roomId The ID of the room
     * @return List of connected rooms
     */
    List<Room> findByNorthRoomIdOrEastRoomIdOrSouthRoomIdOrWestRoomId(
            UUID roomId, UUID roomId1, UUID roomId2, UUID roomId3);

}
