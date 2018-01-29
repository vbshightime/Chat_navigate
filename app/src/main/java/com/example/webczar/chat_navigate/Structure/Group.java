package com.example.webczar.chat_navigate.Structure;

/**
 * Created by webczar on 12/29/2017.
 */

public class Group extends Room {

    public ListFriend listFriends;
    public String id;

    public Group() {
        listFriends = new ListFriend();
    }
}
