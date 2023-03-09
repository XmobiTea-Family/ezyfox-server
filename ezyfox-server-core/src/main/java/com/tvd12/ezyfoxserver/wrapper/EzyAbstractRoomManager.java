package com.tvd12.ezyfoxserver.wrapper;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfox.function.EzyFunctions;
import com.tvd12.ezyfox.util.EzyLoggable;
import com.tvd12.ezyfoxserver.entity.EzyRoom;
import com.tvd12.ezyfoxserver.entity.EzySimpleRoom;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public abstract class EzyAbstractRoomManager extends EzyLoggable implements EzyRoomManager {
    private static final String ROOM_ID_KEY = "_EzyRoomIds";

    @Setter(AccessLevel.NONE)
    protected Map<String, EzyRoom> roomMap = new ConcurrentHashMap<>();

    protected final Map<String, Lock> locks = new ConcurrentHashMap<>();

    private EzyRoom getOrCreateNewRoom(String roomId) {
        EzyRoom answer;

        Lock lock = this.getLock(roomId);
        lock.lock();

        try {
            if (this.roomMap.containsKey(roomId)) {
                answer = this.roomMap.get(roomId);
            } else {
                answer = new EzySimpleRoom(roomId);
                this.roomMap.put(roomId, answer);
            }
        } finally {
            lock.unlock();
            this.removeLock(roomId);
        }

        return answer;
    }

    private List<String> getRoomIdsInPropertiesOf(EzyUser user) {
        List<String> roomIds = user.getProperty(ROOM_ID_KEY);

        if (roomIds == null) {
            roomIds = new ArrayList<>();
            user.setProperty(ROOM_ID_KEY, roomIds);
        }

        return roomIds;
    }

    protected EzyAbstractRoomManager(EzyAbstractRoomManager.Builder<?> builder) {

    }

    @Override
    public List<EzyRoom> getAllRooms() {
        List<EzyRoom> rooms = new ArrayList<>();
        Map<String, EzyRoom> roomMapNow = this.roomMap;
        if (roomMapNow != null) {
            rooms.addAll(roomMapNow.values());
        }
        return rooms;
    }

    @Override
    public void joinRoom(EzyUser user, String roomId) {
        EzyRoom room = this.getOrCreateNewRoom(roomId);
        room.addUser(user);

        List<String> roomIdsInProperties = this.getRoomIdsInPropertiesOf(user);
        if (!roomIdsInProperties.contains(roomId)) {
            roomIdsInProperties.add(roomId);
        }
    }

    @Override
    public void leaveRoom(EzyUser user, String roomId) {
        List<String> roomIdsInProperties = this.getRoomIdsInPropertiesOf(user);

        if (!roomIdsInProperties.contains(roomId)) {
            return;
        }

        if (!this.roomMap.containsKey(roomId)) {
            return;
        }

        EzyRoom room = this.getOrCreateNewRoom(roomId);
        room.removeUser(user);

        if (room.isEmpty()) {
            this.roomMap.remove(roomId);
        }

        roomIdsInProperties.remove(roomId);
    }

    @Override
    public void leaveAllRoom(EzyUser user) {
        List<String> roomIdsInProperties = this.getRoomIdsInPropertiesOf(user);

        for (String roomId : roomIdsInProperties
        ) {
            this.leaveRoom(user, roomId);
        }
    }

    @Override
    public EzyRoom getRoom(String roomId) {
        return this.roomMap.get(roomId);
    }

    @Override
    public List<EzyUser> getUsers(String roomId) {
        EzyRoom room = this.getRoom(roomId);

        List<EzyUser> answer = new ArrayList<>();

        if (room != null) {
            answer.addAll(room.getUsers());
        }

        return answer;
    }

    @Override
    public List<EzyRoom> getRoomsOf(EzyUser user) {
        List<EzyRoom> answer = new ArrayList<>();

        List<String> roomIdsInProperties = this.getRoomIdsInPropertiesOf(user);

        for (String roomId : roomIdsInProperties
        ) {
            EzyRoom channel = this.getRoom(roomId);

            if (channel != null) {
                answer.add(channel);
            }
        }

        return answer;
    }

    @Override
    public Lock getLock(String roomId) {
        return this.locks.computeIfAbsent(roomId, EzyFunctions.NEW_REENTRANT_LOCK_FUNC);
    }

    @Override
    public void removeLock(String roomId) {
        this.locks.remove(roomId);
    }

    @Override
    public void clear() {
        this.locks.clear();
        this.roomMap.clear();
    }

    @Override
    public void clearRoom(String roomId) {
        EzyRoom room = this.getRoom(roomId);

        if (room != null) {
            this.roomMap.remove(roomId);

            List<EzyUser> users = room.getUsers();

            for (EzyUser user : users
            ) {
                List<String> roomIdsInProperties = getRoomIdsInPropertiesOf(user);
                roomIdsInProperties.remove(roomId);
            }


            room.destroy();
        }
    }

    @Override
    public void destroy() {
        this.clear();
    }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends EzyAbstractRoomManager.Builder<B>>
            implements EzyBuilder<EzyRoomManager> {

    }
}
