package com.tvd12.ezyfoxserver.wrapper;

import com.tvd12.ezyfox.util.EzyDestroyable;
import com.tvd12.ezyfoxserver.entity.EzyRoom;
import com.tvd12.ezyfoxserver.entity.EzyUser;

import java.util.List;
import java.util.concurrent.locks.Lock;

public interface EzyRoomManager extends EzyDestroyable {
    List<EzyRoom> getAllRooms();
    
    void join(EzyUser user, String roomId);

    void leave(EzyUser user, String roomId);

    void leaveAll(EzyUser user);

    EzyRoom getRoom(String roomId);

    List<EzyUser> getUsers(String roomId);

    List<EzyRoom> getRoomsOf(EzyUser user);

    Lock getLock(String roomId);

    void removeLock(String roomId);

    void clear();

    void clearRoom(String roomId);
}
