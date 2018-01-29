package com.example.webczar.chat_navigate.Structure;

import java.util.ArrayList;

/**
 * Created by webczar on 12/29/2017.
 */

public class Conversation {
    private ArrayList<Message> arrayMessage;

    public Conversation() {
        arrayMessage = new ArrayList<>();
    }

    public ArrayList<Message> getArrayMessage() {
        return arrayMessage;
    }
}
