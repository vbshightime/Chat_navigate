package com.example.webczar.chat_navigate.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.webczar.chat_navigate.Structure.User;

/**
 * Created by webczar on 12/30/2017.
 */

public class SharedPreferanceHelper {

    private static SharedPreferanceHelper instance = null;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static String MY_PREFERENCES = "userinfo";
    private static String SHARE_KEY_USERNAME = "userName";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_DOB = "userName";
    private static String SHARE_KEY_EMAIL = "email";
    private static String SHARE_KEY_AVATA = "avata";
    private static String SHARE_KEY_UID = "uid";

    public SharedPreferanceHelper() {
    }

    public static SharedPreferanceHelper getInstance(Context context) {

        if (instance == null){
            instance = new SharedPreferanceHelper();
            sharedPreferences = context.getSharedPreferences(MY_PREFERENCES,context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return instance;
    }

    public void saveUserInfo(User user){

        editor.putString(SHARE_KEY_NAME, user.name);
        editor.putString(SHARE_KEY_EMAIL, user.email);
        editor.putString(SHARE_KEY_AVATA,user.avata);
        editor.putString(SHARE_KEY_UID, StaticConfig.UID);
        editor.putString(SHARE_KEY_USERNAME,user.userName);
        editor.putString(SHARE_KEY_DOB,user.dateOfBirth);
        editor.apply();
    }

    public User getUserSavedValue(){
        String userName = sharedPreferences.getString(SHARE_KEY_NAME, "");
        String email = sharedPreferences.getString(SHARE_KEY_EMAIL, "");
        String avatar = sharedPreferences.getString(SHARE_KEY_AVATA, "default");
        String fullName = sharedPreferences.getString(SHARE_KEY_USERNAME,"");
        String birthDate = sharedPreferences.getString(SHARE_KEY_DOB,"");


        User user = new User();
        user.name = userName;
        user.email = email;
        user.avata = avatar;
        user.userName = fullName;
        user.dateOfBirth = birthDate;

        return user;
    }

    public String getUid(){
        return sharedPreferences.getString(SHARE_KEY_UID,"");
    }

}
