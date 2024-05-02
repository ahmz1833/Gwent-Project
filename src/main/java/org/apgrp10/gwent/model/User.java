package org.apgrp10.gwent.model;

import java.util.*;

public class User {
    private static final ArrayList<User> allUsers = new ArrayList<>();
    private String username, password, nickname, email;
    private static User currentUser = null;

    public User(String username, String password, String nickname, String email) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        allUsers.add(this);
    }

    //set and get functions:
    public static boolean haveUserWithUsername(String username) {
        for (User user : allUsers)
            if (user.username.equals(username))
                return true;
        return false;
    }

    public static User getUserByName(String username) {
        for (User user : allUsers)
            if (user.username.equals(username))
                return user;
        return null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    //end of set and get functions
}
