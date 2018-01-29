package com.example.webczar.chat_navigate.Services;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.icu.text.DateFormat;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.example.webczar.chat_navigate.Helper.SharedPreferanceHelper;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.Structure.Friend;
import com.example.webczar.chat_navigate.Structure.ListFriend;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by webczar on 12/30/2017.
 */

public class ServiceUtils {

    private static ServiceConnection serviceStart;
    private static ServiceConnection serviceDestroyed;

    public static boolean isServiceRunning(Context context){
        Class<?> serviceClass = FriendChatService.class;
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if (serviceClass.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    public static void stopTheService(Context context){
        if (isServiceRunning(context)){
            Intent intent = new Intent(context,FriendChatService.class);
            if (serviceDestroyed != null){
                context.unbindService(serviceDestroyed);
            }
            serviceDestroyed = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                 FriendChatService.LocalBinder binder = (FriendChatService.LocalBinder) service;
                    binder.getService().stopSelf();
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            context.bindService(intent,serviceDestroyed,context.BIND_NOT_FOREGROUND);
        }
    }

    public static void stopRoom(Context context, final String idRoom){
        if (isServiceRunning(context)){
            Intent intent =new Intent(context,FriendChatService.class);
            if (serviceDestroyed != null){
                context.unbindService(serviceDestroyed);
                serviceDestroyed = null;
            }
            serviceDestroyed = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    FriendChatService.LocalBinder binder = (FriendChatService.LocalBinder) service;
                    binder.getService().stopNotify(idRoom);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
            context.bindService(intent,serviceDestroyed,context.BIND_NOT_FOREGROUND);
        }
    }


    public static void startTheService(Context context){
        if (!isServiceRunning(context)){
            Intent intent = new Intent(context,FriendChatService.class);
            context.startService(intent);
        }else {
            if (serviceStart != null){
                context.unbindService(serviceStart);
            }
            serviceStart = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                 FriendChatService.LocalBinder binder = (FriendChatService.LocalBinder) service;
                    for (Friend friend: binder.getService().listFriend.getListFriend()){
                        binder.getService().mapMark.put(friend.idRoom,true);
                    }
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
            Intent intent = new Intent(context,FriendChatService.class);
            context.bindService(intent,serviceStart,context.BIND_NOT_FOREGROUND);
        }
    }

    public static void updateUserStatus(Context context){
        if (isNetworkConnected(context)){
            String uid = SharedPreferanceHelper.getInstance(context).getUid();
            if (uid != ""){
                FirebaseDatabase.getInstance().getReference().child("user/"+ uid + "status/isOnline").setValue(true);
                FirebaseDatabase.getInstance().getReference().child("user/"+ uid + "status/timestamp").setValue(System.currentTimeMillis());
            }
        }
    }

    public static void updateFriendStatus(Context context, ListFriend listFriend){
        if (isNetworkConnected(context)){
            for (Friend friend: listFriend.getListFriend()) {
                final String friendId = friend.id;
                FirebaseDatabase.getInstance().getReference().child("user/" + friendId + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null){
                            HashMap mapStatus = (HashMap) dataSnapshot.getValue();
                            if ((boolean) mapStatus.get("isOnline") && (System.currentTimeMillis() - (long) mapStatus.get("timestamp")) > StaticConfig.TIME_TO_OFFLINE) {
                                FirebaseDatabase.getInstance().getReference().child("user/" + friendId + "/status/isOnline").setValue(false);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private static boolean isNetworkConnected(Context context) { try{
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }catch (Exception e){
        return true;
    }
    }


}
