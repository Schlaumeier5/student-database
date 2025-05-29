package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class Room {
    private static final String[] SQL_FIELDS = {"label", "minimum_level"};
    private static final Map<String, Room> rooms = new HashMap<>();
    private final String label;
    private final int minimumLevel;

    private Room(String label, int minimumLevel) {
        this.label = label;
        this.minimumLevel = minimumLevel;
    }

    private static Room fromSQLFields(String[] sqlResult) {
        String label = sqlResult[0];
        int minimumLevel = Integer.parseInt(sqlResult[1]);
        return new Room(label, minimumLevel);
    }

    public static void fetchAll() throws SQLException {
        Server.getInstance().processRequest((fields) -> {
            Room room = fromSQLFields(fields);
            rooms.put(room.getLabel(), room);
        }, "get_all_rooms", SQL_FIELDS);
    }
    public static void fetchAllIfNotExists() throws SQLException {
        if (rooms.size() == 0) {
            fetchAll();
        }
    }

    public static Map<String, Room> getRooms() {
        try {
            fetchAllIfNotExists();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not fetch rooms", e);
        }
        return rooms;
    }
    public static Room getRoom(String label) {
        if (rooms.keySet().contains(label)) {
            return rooms.get(label);
        } else {
            try {
                Room room = Server.getInstance().processSingleRequest(Room::fromSQLFields, "get_room_by_label", SQL_FIELDS, label);
                rooms.put(label, room);
                return room;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        }
    }
    public String getLabel() {
        return label;
    }
    public int getMinimumLevel() {
        return minimumLevel;
    }
    @Override
    public String toString() {
        return "{\"label\": \""+label+ "\", \"minimumLevel\": \"" + minimumLevel + "\"}";
    }
    public static void addRoom(String label, int minimumLevel) throws SQLException {
        Room room = new Room(label, minimumLevel);
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("room", label, String.valueOf(minimumLevel)));
        rooms.put(label, room);
    }
}
