package com.example.webczar.chat_navigate.Fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webczar.chat_navigate.Activities.Chatctivity;
import com.example.webczar.chat_navigate.DataBase.FriendDB;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.MainActivity;
import com.example.webczar.chat_navigate.R;
import com.example.webczar.chat_navigate.Services.ServiceUtils;
import com.example.webczar.chat_navigate.Structure.Friend;
import com.example.webczar.chat_navigate.Structure.ListFriend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.v7.widget.LinearLayoutManager.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class Friends_fragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public RecyclerView recyclerView;
    private FriendsListAdapter adapter;
    private ListFriend dataListFriend = null;
    private CountDownTimer countDownTimer;
    private FloatingActionButton floatingActionButton;
    private ArrayList<String> idFriends = null;
    private AlertDialog alertDialog;
    public static final int START_CHAT_ID = 1100;
    public static final String ACTION_DELETE_FRIEND = "com.example.webczar.chat_navigate.DELETE_FRIEND";
    private SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver receiver;
    private ProgressBar progressBar;
    private boolean isUserName;

    public Friends_fragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /*private void addFriend() {
    startActivity(new Intent(getContext(),Chatctivity.class));
    }*/

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_friends);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflator = LayoutInflater.from(getContext());
                v = inflator.inflate(R.layout.edit_add_friend, null, false);
                final EditText editText = (EditText) v.findViewById(R.id.dial_addFriend);
                new AlertDialog.Builder(getContext()).setView(v).setTitle("Add Friend")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String friendEmailOrUserName = editText.getText().toString().trim();
                                Pattern VALID_EMAIL_ADDRESS_REGEX =
                                        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

                                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(friendEmailOrUserName);
                                boolean matchFound = matcher.find();
                                if (matchFound == false) {
                                    Log.d(MainActivity.class.getSimpleName(),"match not found");
                                    isUserName = true;
                                    findIdEmail(friendEmailOrUserName,isUserName);
                                    Toast.makeText(getContext(), "Please Enter correct Email", Toast.LENGTH_SHORT);

                                }else {
                                    isUserName = false;
                                    findIdEmail(friendEmailOrUserName,isUserName);
                                }
                            }

                            private void findIdEmail(String emailFriend, boolean isUsername) {
                                if (isUsername == false){
                                    FirebaseDatabase.getInstance().getReference().child("user").orderByChild("email").equalTo(emailFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() == null){
                                                Toast.makeText(getContext(),"Enter Correct Email",Toast.LENGTH_SHORT).show();

                                            }else {
                                                String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                                                if (id.equals(StaticConfig.UID)) {
                                                    Toast.makeText(getContext(),"Email Not Valid",Toast.LENGTH_SHORT).show();
                                                } else {
                                                    HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
                                                    Friend user = new Friend();
                                                    user.name = (String) userMap.get("name");
                                                    user.email = (String) userMap.get("email");
                                                    user.avata = (String) userMap.get("avata");
                                                    user.id = id;
                                                    user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();

                                                    checkBeforAddFriend(id, user);
                                                }

                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d("Friends_fragment","Database Error"+ databaseError.toString());
                                        }
                                    });
                                }else if(isUsername == true) {
                                    FirebaseDatabase.getInstance().getReference().child("user").orderByChild("name").equalTo(emailFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() == null){
                                                Toast.makeText(getContext(),"Enter Correct Email or Username",Toast.LENGTH_SHORT);
                                            }else {
                                                String idUserName = ((HashMap)dataSnapshot.getValue()).keySet().iterator().next().toString();
                                                        if (idUserName == StaticConfig.UID){
                                                            Toast.makeText(getContext(),"USerName is not valid", Toast.LENGTH_SHORT);
                                                        }else {
                                                            HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(idUserName);
                                                            Friend user = new Friend();
                                                            user.name = (String) userMap.get("name");
                                                            user.email = (String) userMap.get("email");
                                                            user.avata = (String) userMap.get("avata");
                                                            user.id = idUserName;
                                                            user.idRoom = idUserName.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + idUserName).hashCode() + "" : "" + (idUserName + StaticConfig.UID).hashCode();
                                                            checkBeforAddFriend(idUserName, user);
                                                        }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                            }

                            private void checkBeforAddFriend(String idFriend, Friend userInfo) {

                                if (idFriends.contains(idFriend)) {
                                    Toast.makeText(getContext(),"Friends", Toast.LENGTH_SHORT);
                                } else {
                                    addFriend(idFriend, true);
                                    idFriends.add(idFriend);
                                    dataListFriend.getListFriend().add(userInfo);
                                    FriendDB.getInstance(getContext()).addFriend(userInfo);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            private void addFriend(final String idFriend, boolean isIdFriend) {
                                if (idFriend != null) {
                                    if (isIdFriend) {
                                        //if isIdFriend is true then push the value of uid and instead of it set the value of idFriend
                                        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).push().setValue(idFriend)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            //put value of boolean as false
                                                            addFriend(idFriend, false);
                                                            //now the loop will go to next else statement
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(),"Failed to add friend",Toast.LENGTH_SHORT);
                                                    }
                                                });
                                    } else {

                                        FirebaseDatabase.getInstance().getReference().child("friend/" + idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    addFriend(null, false);
                                                }
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(getContext(),"Friend Successfuly added", Toast.LENGTH_SHORT);
                                }

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

            }
        });

        //adapter.itemTouchHelper.attachToRecyclerView(recyclerView);

        countDownTimer = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long millisUntilFinished) {
                ServiceUtils.updateUserStatus(getContext());
                ServiceUtils.updateFriendStatus(getContext(), dataListFriend);
            }

            @Override
            public void onFinish() {

            }
        };

        if (dataListFriend == null) {
            dataListFriend = FriendDB.getInstance(getContext()).getListFriend();
            if (dataListFriend.getListFriend().size() > 0) {
                idFriends = new ArrayList<String>();
                for (Friend friend : dataListFriend.getListFriend()) {
                    idFriends.add(friend.id);
                }
                countDownTimer.start();

            }
        }

        adapter = new FriendsListAdapter(dataListFriend,getContext(),this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        if (idFriends == null) {
            idFriends = new ArrayList<>();
            LayoutInflater inflate = LayoutInflater.from(getContext());
            View view_add_friends = inflate.inflate(R.layout.edit_progress, null);
            progressBar = (ProgressBar) view_add_friends.findViewById(R.id.progress_adding_friends);


            alertDialog = new AlertDialog.Builder(getContext())
                    .setCancelable(true)
                    .setView(view_add_friends)
                    .setTitle("Loading Friends").create();
            alertDialog.show();

            progressBar.setVisibility(view.VISIBLE);
            getListFriendUId();
            alertDialog.dismiss();
        }
//creating broadcast receiver to delete friend
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDelete = intent.getExtras().getString("idFriend");
                for (Friend friend : dataListFriend.getListFriend()) {
                    if (idDelete == friend.id) {
                        ArrayList<Friend> friends = dataListFriend.getListFriend();
                        friends.remove(friend);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };

        //adding action to broadcast receiver
        IntentFilter filter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(receiver, filter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_CHAT_ID && data!=null &&FriendsListAdapter.mapMark != null){
            FriendsListAdapter.mapMark.put(data.getStringExtra("idFriend"),false);
        }
    }

    @Override
    public void onRefresh() {
        idFriends.clear();
        dataListFriend.getListFriend().clear();
        FriendDB.getInstance(getContext()).dropDB();
        countDownTimer.cancel();
        getListFriendUId();
    }


    public void getListFriendUId() {

        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    //create iterator and iterate through HashMap collection
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        idFriends.add(mapRecord.get(key).toString());
                    }
                    getAllFriendInfo(0);
                } else {
                    alertDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAllFriendInfo(final int index) {
        if (index == idFriends.size()) {
            adapter.notifyDataSetChanged();
            //notifydatasetchange
            swipeRefreshLayout.setRefreshing(false);
            countDownTimer.start();
        } else {
            final String id = idFriends.get(index);
            FirebaseDatabase.getInstance().getReference().child("user/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        //get the value tp user name class
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.id = id;
                        //how we did this I don't know
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : (id + StaticConfig.UID).hashCode() + "";
                        dataListFriend.getListFriend().add(user);
                        FriendDB.getInstance(getContext()).addFriend(user);
                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}

class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsListHolder> {
    private ListFriend listFriend ;
    private Context context;
    public ItemTouchHelper itemTouchHelper;
    public static Map<String, Query> mapQueryUser;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    public AlertDialog.Builder alertDialogDelete;
    private Friends_fragment fragment;

    public FriendsListAdapter(ListFriend listFriend, Context context, Friends_fragment fragment) {
        this.listFriend = listFriend;
        this.context = context;
        this.fragment = fragment;
        mapMark = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryUser = new HashMap<>();
    }



    @Override
    public FriendsListAdapter.FriendsListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(context);
        View view = inflator.inflate(R.layout.rc_item_friend, null);
        return new FriendsListHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final FriendsListAdapter.FriendsListHolder holder, final int position) {
        final String name = listFriend.getListFriend().get(position).name;
        final String id = listFriend.getListFriend().get(position).id;
        final String idRoom = listFriend.getListFriend().get(position).idRoom;
        final String avata = listFriend.getListFriend().get(position).avata;

        ((FriendsListHolder) holder).textName.setText(name);
        ((FriendsListHolder) holder).textName.setTypeface(Typeface.DEFAULT);
        ((FriendsListHolder) holder).textMessage.setTypeface(Typeface.DEFAULT);

        ((View) ((FriendsListHolder) holder).textName.getParent().getParent().getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Chatctivity.class);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, name);
                ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
                idFriend.add(id);
                Chatctivity.bitmapAvataFriend = new HashMap<>();
                if (!avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                    byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                    Chatctivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                } else {
                    Chatctivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                }

                Chatctivity.userReceiver = new HashMap<>();
                Chatctivity.userReceiver.put(id, name);
                intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);

                //ToDo:decode avatar string in Chat Activity
                mapMark.put(id, null);
                fragment.startActivityForResult(intent,Friends_fragment.START_CHAT_ID);
            }
        });

         ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String idname = ((FriendsListHolder)holder).textName.getText().toString();
                final String friendRemove = listFriend.getListFriend().get(position).id;
                deleteFriend(friendRemove,idname);

            }
        });

        if (listFriend.getListFriend().get(position).message.text.length() > 0) {
            ((FriendsListHolder) holder).textMessage.setVisibility(View.VISIBLE);
            ((FriendsListHolder) holder).textName.setVisibility(View.VISIBLE);
            if (!listFriend.getListFriend().get(position).message.text.startsWith(id)) {
                ((FriendsListHolder) holder).textMessage.setText(listFriend.getListFriend().get(position).message.text);
                ((FriendsListHolder) holder).textName.setTypeface(Typeface.DEFAULT);
                holder.textMessage.setTypeface(Typeface.DEFAULT);
            } else {
                ((FriendsListHolder) holder).textMessage.setText(listFriend.getListFriend().get(position).message.text.substring((id + "").length()));
                ((FriendsListHolder) holder).textMessage.setTypeface(Typeface.DEFAULT_BOLD);
                ((FriendsListHolder) holder).textName.setTypeface(Typeface.DEFAULT_BOLD);
            }

            String time = new SimpleDateFormat("EEE d MM yyyy").format(new Date(listFriend.getListFriend().get(position).message.timestamp));
            String today = new SimpleDateFormat("EEE d MM yyyy").format(new Date(System.currentTimeMillis()));

            if (time.equals(today)) {
                ((FriendsListHolder) holder).textTime.setText(new SimpleDateFormat("HH:mm").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
            } else {
                ((FriendsListHolder) holder).textTime.setText(new SimpleDateFormat("MMM d").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
            }
        } else {
            ((FriendsListHolder) holder).textMessage.setVisibility(View.GONE);
            ((FriendsListHolder) holder).textName.setVisibility(View.GONE);

            if (mapMark.get(id) == null && mapChildListenerOnline.get(id) == null) {
                //if mapmark and listener is null then query to get
                mapQueryUser.put(id, FirebaseDatabase.getInstance().getReference().child("message/" + idRoom).limitToLast(1));

                //now call the listener to add value to child
                mapChildListenerOnline.put(id, new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //create hashmap to get the value from data snapshot to be latter stored other hash map
                        HashMap messageMap = (HashMap) dataSnapshot.getValue();
                        //if id value is present in map
                        if (mapMark.get(id) != null) {
                            if (!mapMark.get(id)) {
                                //if value is not there then set text appending id
                                listFriend.getListFriend().get(position).message.text = id + messageMap.get("text");
                            } else {
                                //if value of that articular id is present then
                                listFriend.getListFriend().get(position).message.text = (String) messageMap.get("text");
                            }
                            notifyDataSetChanged();
                            mapMark.put(id, false);
                        } else {
                            listFriend.getListFriend().get(position).message.text = (String) messageMap.get("text");
                            notifyDataSetChanged();
                        }
                        listFriend.getListFriend().get(position).message.timestamp = (long) messageMap.get("timestamp");
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
                mapQueryUser.get(id).addChildEventListener(mapChildListenerOnline.get(id));
                mapMark.put(id, true);
            } else {
                //removethe listener
                mapQueryUser.get(id).removeEventListener(mapChildListenerOnline.get(id));
                mapQueryUser.get(id).addChildEventListener(mapChildListenerOnline.get(id));
                mapMark.put(id, true);
            }
        }
        if (listFriend.getListFriend().get(position).avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            ((FriendsListHolder) holder).imageView.setImageResource(R.drawable.default_avata);
        } else {
            byte[] decodedString = Base64.decode(listFriend.getListFriend().get(position).avata, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((FriendsListHolder) holder).imageView.setImageBitmap(src);
        }

        //get the user status if he is online

        if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
            mapQueryOnline.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/status"));
            mapChildListenerOnline.put(id, new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey() == "isOnline") {
                        listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey() == "isOnline") {
                        listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
                        notifyDataSetChanged();
                    }

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
            mapQueryOnline.get(id).addChildEventListener(mapChildListenerOnline.get(id));
        }
        if (listFriend.getListFriend().get(position).status.isOnline) {
            ((FriendsListHolder) holder).imageView.setBorderWidth(10);
        } else {
            ((FriendsListHolder) holder).imageView.setBorderWidth(0);
        }


    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;
    }


    public void deleteFriend(final String idFriend, String idName){
        alertDialogDelete = new AlertDialog.Builder(context);
        //ToDo:Add friend name instead of just friend
        alertDialogDelete.setTitle("Delete"+ idName);
        alertDialogDelete.setMessage("Are You Sure!!");

        if (idFriend != null){
        alertDialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID).
                        orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null){
                            dialog.dismiss();
                        }
                        else {
                            //find id from data snapshot and iterate to get the correct value
                            final String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                            //now remove the user by removeUser() query
                            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID).child(idRemoval).removeValue().
                                    addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //send broadcast to broadcast receiver about completing the task
                                            Intent intentDelete = new Intent(Friends_fragment.ACTION_DELETE_FRIEND);
                                            intentDelete.putExtra("idFriend", idFriend);
                                            context.sendBroadcast(intentDelete);
                                        }
                                    }).addOnFailureListener((Activity) context, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
            alertDialogDelete.create().show();
        }else {
            alertDialogDelete.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
        }

    }

        class FriendsListHolder extends RecyclerView.ViewHolder {
            public CircleImageView imageView;
            public TextView textMessage,textTime,textName;
            public Context context;

            FriendsListHolder(Context context,View itemView) {
                super(itemView);
                imageView = (CircleImageView) itemView.findViewById(R.id.icon_avata);
                textName = (TextView) itemView.findViewById(R.id.txtName);
                textTime = (TextView) itemView.findViewById(R.id.txtTime);
                textMessage = (TextView) itemView.findViewById(R.id.txtMessage);
                this.context = context;
            }
        }
}


