package com.example.webczar.chat_navigate.Structure;

import android.os.Message;

/**
 * Created by webczar on 12/29/2017.
 */

public class User {
    public String name;
    public String email;
    public String userName;
    public String dateOfBirth;
    public com.example.webczar.chat_navigate.Structure.Message message;
    public Status status;
    public String avata;



    public User() {

        message = new com.example.webczar.chat_navigate.Structure.Message();
        status = new Status();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }
}
