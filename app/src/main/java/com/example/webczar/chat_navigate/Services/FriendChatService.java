package com.example.webczar.chat_navigate.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.webczar.chat_navigate.Activities.Chatctivity;
import com.example.webczar.chat_navigate.DataBase.FriendDB;
import com.example.webczar.chat_navigate.DataBase.GroupDB;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.MainActivity;
import com.example.webczar.chat_navigate.R;
import com.example.webczar.chat_navigate.Structure.Friend;
import com.example.webczar.chat_navigate.Structure.Group;
import com.example.webczar.chat_navigate.Structure.ListFriend;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by webczar on 12/30/2017.
 */

public class FriendChatService extends Service {
    private static String TAG = "FriendChatService";
    // Binder given to clients
    public final IBinder mBinder = new LocalBinder();
    public Map<String, Boolean> mapMark;
    public Map<String, Query> mapQuery;
    public Map<String, ChildEventListener> mapChildEventListenerMap;
    public Map<String, Bitmap> mapBitmap;
    public ArrayList<String> listKey;
    public ListFriend listFriend;
    public ArrayList<Group> listGroup;
    public CountDownTimer updateOnline;


    @Override
    public void onCreate() {
        super.onCreate();

        mapMark = new HashMap<>();
        mapQuery = new HashMap<>();
        mapChildEventListenerMap = new HashMap<>();
        listFriend = FriendDB.getInstance(this).getListFriend();
        listGroup = GroupDB.getInstance(this).getListGroups();
        listKey = new ArrayList<>();
        mapBitmap = new HashMap<>();
        updateOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                ServiceUtils.updateUserStatus(getApplicationContext());
            }

            @Override
            public void onFinish() {

            }
        };
        updateOnline.start();

        if (listFriend.getListFriend().size() > 0 || listGroup.size() > 0) {

            for (final Friend friend : listFriend.getListFriend()) {
                if (!listKey.contains(friend.idRoom)) {
                    mapQuery.put(friend.idRoom, FirebaseDatabase.getInstance().getReference().child("message/" + friend.idRoom).limitToLast(1));
                    mapChildEventListenerMap.put(friend.idRoom, new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (mapMark.get(friend.idRoom) != null && mapMark.get(friend.idRoom)) {
                              Toast.makeText(FriendChatService.this, friend.name + ": " + ((HashMap)dataSnapshot.getValue()).get("text"), Toast.LENGTH_SHORT).show();
                                if (mapBitmap.get(friend.idRoom) == null) {
                                    if (!friend.avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                                        byte[] decodedString = Base64.decode(friend.avata, Base64.DEFAULT);
                                        mapBitmap.put(friend.idRoom, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                    } else {
                                        mapBitmap.put(friend.idRoom, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_default_avata));
                                    }
                                }
                                createNotify(friend.name, (String) ((HashMap) dataSnapshot.getValue()).get("text"), friend.idRoom.hashCode(), mapBitmap.get(friend.idRoom), false);

                            } else {
                                mapMark.put(friend.idRoom, true);
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }


                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    listKey.add(friend.idRoom);
                }
                mapQuery.get(friend.idRoom).addChildEventListener(mapChildEventListenerMap.get(friend.idRoom));
            }

            for (final Group group : listGroup) {
                if (!listKey.contains(group.id)) {
                    mapQuery.put(group.id, FirebaseDatabase.getInstance().getReference().child("message/" + group.id).limitToLast(1));
                    mapChildEventListenerMap.put(group.id, new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (mapMark.get(group.id) != null && mapMark.get(group.id)) {
                                if (mapBitmap.get(group.id) == null) {
                                    mapBitmap.put(group.id, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_group_avata));
                                }
                                createNotify(group.groupInfo.get("name"), (String) ((HashMap) dataSnapshot.getValue()).get("text"), group.id.hashCode(), mapBitmap.get(group.id) , true);
                            } else {
                                mapMark.put(group.id, true);
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    listKey.add(group.id);
                }
                mapQuery.get(group.id).addChildEventListener(mapChildEventListenerMap.get(group.id));
            }

        } else {
            stopSelf();
        }
    }

    private static PendingIntent commonIntent(Context context){
        Intent intentMainView = new Intent(context, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intentMainView);
        Intent intentMessageView = new Intent(context, Chatctivity.class);
        taskStackBuilder.addNextIntent(intentMessageView);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_ONE_SHOT);
        return pendingIntent;
    }

    private void createNotify(String name, String content, int id, Bitmap icon, boolean isGroup) {
        NotificationCompat.Builder notificationBuilder = new
                NotificationCompat.Builder(this)
                .setLargeIcon(icon)
                .setContentTitle(name)
                .setContentText(content)
                .setContentIntent(commonIntent(this))
                .setVibrate(new long[] { 1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true);
        if (isGroup) {
            notificationBuilder.setSmallIcon(R.drawable.ic_group_notify);
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_person_notify);
        }
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(
                        Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
        notificationManager.notify(id,
                notificationBuilder.build());
    }

    public void stopNotify(String id) {
        mapMark.put(id, false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"services started");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (String id : listKey) {
            mapQuery.get(id).removeEventListener(mapChildEventListenerMap.get(id));
        }
        mapQuery.clear();
        mapChildEventListenerMap.clear();
        mapBitmap.clear();
        updateOnline.cancel();
        Log.d(TAG, "OnDestroyService");
    }
    public class LocalBinder extends Binder {

        public FriendChatService getService(){
            return FriendChatService.this;
        }
    }
}
