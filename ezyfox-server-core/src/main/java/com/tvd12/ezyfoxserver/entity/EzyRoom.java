package com.tvd12.ezyfoxserver.entity;

import com.tvd12.ezyfox.util.EzyDestroyable;

import java.util.List;
import java.util.concurrent.locks.Lock;

public interface EzyRoom extends EzyDestroyable {

    /**
     * Get room id.
     *
     * @return the room id
     */
    String getRoomId();

    /**
     * Get the lock of the room.
     *
     * @param name the lock name
     * @return the lock
     */
    Lock getLock(String name);

    /**
     * Get current users.
     *
     * @return the current users
     */
    List<EzyUser> getUsers();

    /**
     * Get current username.
     *
     * @return the current username
     */
    List<String> getUsernames();

    /**
     * Add new user.
     *
     * @param user the user to add
     */
    void addUser(EzyUser user);

    /**
     * remove a user.
     *
     * @param user the user to remove
     */
    void removeUser(EzyUser user);

    /**
     * check the room contains user.
     *
     * @param user the user need check
     * @return is user in the room
     */
    boolean containsUser(EzyUser user);

    /**
     * check the room contains user via username.
     *
     * @param username the username of user need check
     * @return is username in the room
     */
    boolean containsUser(String username);

    /**
     * how many user in this room.
     *
     * @return the user count
     */
    int size();

    /**
     * The room is empty or not.
     *
     * @return is empty or not
     */
    boolean isEmpty();
}
