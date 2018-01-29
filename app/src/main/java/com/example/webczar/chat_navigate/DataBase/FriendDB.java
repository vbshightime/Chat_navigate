package com.example.webczar.chat_navigate.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.webczar.chat_navigate.Structure.Friend;
import com.example.webczar.chat_navigate.Structure.ListFriend;

import java.security.AccessControlContext;

import static android.R.attr.version;

/**
 * Created by webczar on 12/30/2017.
 */

public final class FriendDB {

    private static FriendDBHelper friendDBHelper = null;
    private static FriendDB instance = null;

    public FriendDB() {
    }

    public static FriendDB getInstance(Context context){
        if (instance == null){
            friendDBHelper = new FriendDBHelper(context);
            instance = new FriendDB();
        }
        return instance;
    }

    public class ContractDB implements BaseColumns{
        static final String TABLE_NAME = "friend";
        static final String COLUMN_NAME_ID = "friendID";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_USERNAME = "userName";
        static final String COLUMN_NAME_EMAIL = "email";
        static final String COLUMN_NAME_ID_ROOM = "idRoom";
        static final String COLUMN_NAME_AVATA = "avata";
        static final String COLUMN_NAME_DOB = "dob";
    }

    public long addFriend(Friend friend){
        SQLiteDatabase database = friendDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContractDB.COLUMN_NAME_ID, friend.id);
        values.put(ContractDB.COLUMN_NAME_NAME, friend.name);
        values.put(ContractDB.COLUMN_NAME_EMAIL, friend.email);
        values.put(ContractDB.COLUMN_NAME_ID_ROOM, friend.idRoom);
        values.put(ContractDB.COLUMN_NAME_AVATA, friend.avata);
        values.put(ContractDB.COLUMN_NAME_USERNAME,friend.userName);
        values.put(ContractDB.COLUMN_NAME_DOB,friend.dateOfBirth);

        // Insert the new row, returning the primary key value of the new row
        return database.insert(ContractDB.TABLE_NAME, null, values);
    }

    public void addListFriend(ListFriend listFriend){
        for(Friend friend: listFriend.getListFriend()){
            addFriend(friend);
        }
    }

    public ListFriend getListFriend(){
        ListFriend listFriend = new ListFriend();
        SQLiteDatabase database = friendDBHelper.getReadableDatabase();
        try {
            Cursor cursor = database.rawQuery("select * from " + ContractDB.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                Friend friend = new Friend();
                friend.id = cursor.getString(0);
                friend.name = cursor.getString(1);
                friend.email = cursor.getString(2);
                friend.idRoom = cursor.getString(3);
                friend.avata = cursor.getString(4);
                friend.userName = cursor.getString(5);
                friend.dateOfBirth = cursor.getString(6);
                listFriend.getListFriend().add(friend);
            }
            cursor.close();
        }catch (Exception e){
            return new ListFriend ();
        }
        return listFriend;
    }

    public void dropDB(){
        SQLiteDatabase db = friendDBHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ContractDB.TABLE_NAME + " (" +
                    ContractDB.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    ContractDB.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    ContractDB.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    ContractDB.COLUMN_NAME_ID_ROOM + TEXT_TYPE + COMMA_SEP +
                    ContractDB.COLUMN_NAME_AVATA + TEXT_TYPE + COMMA_SEP +
                    ContractDB.COLUMN_NAME_USERNAME +TEXT_TYPE + COMMA_SEP+
                    ContractDB.COLUMN_NAME_DOB+ TEXT_TYPE +" )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ContractDB.TABLE_NAME;



    private static class FriendDBHelper extends SQLiteOpenHelper {

        static final int DATABASE_VERSION = 4;
        static final String DATABASE_NAME = "FriendChat.db";

        public FriendDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
