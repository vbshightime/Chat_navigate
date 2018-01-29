package com.example.webczar.chat_navigate.Structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by webczar on 12/29/2017.
 */

public class Room {

    public ArrayList<String> member;
    public Map<String, String> groupInfo;

    public Room(){
        member = new ArrayList<>();
        groupInfo = new HashMap<String, String>();
    }
}
