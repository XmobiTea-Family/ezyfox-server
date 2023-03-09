package com.tvd12.ezyfoxserver.entity;

import com.tvd12.ezyfox.function.EzyFunctions;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class EzySimpleRoom implements EzyRoom {
    protected final String roomId;

    @Setter(AccessLevel.NONE)
    protected Map<Long, EzyUser> userMap = new ConcurrentHashMap<>();

    @Setter(AccessLevel.NONE)
    protected Map<String, Lock> locks = new ConcurrentHashMap<>();

    @Override
    public String getRoomId() {
        return this.roomId;
    }

    @Override
    public Lock getLock(String name) {
        return this.locks.computeIfAbsent(name, EzyFunctions.NEW_REENTRANT_LOCK_FUNC);
    }

    @Override
    public List<EzyUser> getUsers() {
        List<EzyUser> users = new ArrayList<>();
        Map<Long, EzyUser> userMapNow = this.userMap;
        if (userMapNow != null) {
            users.addAll(userMapNow.values());
        }
        return users;
    }

    @Override
    public List<String> getUsernames() {
        List<EzyUser> users = this.getUsers();

        List<String> answers = new ArrayList<>();

        for (EzyUser ezyUser : users
             ) {
            answers.add(ezyUser.getName());
        }

        return answers;
    }

    @Override
    public void addUser(EzyUser user) {
        this.userMap.putIfAbsent(user.getId(), user);
    }

    @Override
    public void removeUser(EzyUser user) {
        this.userMap.remove(user.getId());
    }

    @Override
    public int size() {
        return this.userMap.size();
    }

    @Override
    public boolean containsUser(EzyUser user) {
        return this.userMap.containsKey(user.getId());
    }

    @Override
    public boolean containsUser(String username) {
        List<String> usernames = this.getUsernames();
        return usernames.contains(username);
    }

    @Override
    public boolean isEmpty() {
        return this.userMap.isEmpty();
    }

    @Override
    public void destroy() {
        if (this.locks != null) {
            this.locks.clear();
        }
        if (this.userMap != null) {
            this.userMap.clear();
        }

        this.locks = null;
        this.userMap = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof EzySimpleRoom) {
            return roomId == ((EzySimpleRoom) obj).roomId;
        }
        return false;
    }

    @Override
    public String toString() {
        return roomId + ", size: " + this.size();
    }

    public EzySimpleRoom(String roomId) {
        this.roomId = roomId;
    }
}
