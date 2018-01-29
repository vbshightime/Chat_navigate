package com.example.webczar.chat_navigate.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.webczar.chat_navigate.Helper.SharedPreferanceHelper;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.R;
import com.example.webczar.chat_navigate.Structure.Conversation;
import com.example.webczar.chat_navigate.Structure.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chatctivity extends AppCompatActivity implements View.OnClickListener{

    public static final int USER_MESSAGE_ID = 0;
    public static final int FRIEND_MESSAGE_ID =1;
    private RecyclerView recyclerView;
    private String roomId;
    private Toolbar toolbar;
    public chatAdapter adapter;
    private Conversation conversation;
    private EditText writeMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String,Bitmap> bitmapAvataFriend;
    public static HashMap<String,String> userReceiver;
    private ArrayList<CharSequence> idFriend;
    public Bitmap bitmapAvataUser;
    public String userMessageName;
    private Button btnSnd;
    private ImageView image_in_toolbar;
    private TextView text_in_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatctivity);
        Intent intent = getIntent();
        idFriend = intent.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        roomId = intent.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        String friendName = intent.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);
        btnSnd = (Button) findViewById(R.id.btnSend);
        btnSnd.setOnClickListener(this);
        conversation = new Conversation();

        //setUpToolbar();

        SharedPreferanceHelper helper = SharedPreferanceHelper.getInstance(this);
        String userAvata = helper.getUserSavedValue().avata;
        userMessageName = helper.getUserSavedValue().name;
        if (!userAvata.equals(StaticConfig.STR_DEFAULT_BASE64)){
            byte[] decodeString = Base64.decode(userAvata,Base64.DEFAULT);
            bitmapAvataUser = BitmapFactory.decodeByteArray(decodeString,0,decodeString.length);
        }else {
            Resources res = getResources();
            bitmapAvataUser = BitmapFactory.decodeResource(res,R.drawable.default_avata);
        }

        writeMessage = (EditText) findViewById(R.id.edit_message);
        if (idFriend!= null ){
            adapter = new chatAdapter(this,conversation,bitmapAvataFriend,bitmapAvataUser,userMessageName,userReceiver);
            //ToDo: getSupportActionBar().setTitle(friendName);
            linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
            recyclerView = (RecyclerView) findViewById(R.id.recycler_chat);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            FirebaseDatabase.getInstance().getReference().child("message/"+roomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue()!=null){
                        HashMap hashMap = (HashMap) dataSnapshot.getValue();
                        Message newMessage  = new Message();
                        newMessage.idReceiver = (String) hashMap.get("idReceiver");
                        newMessage.idSender = (String) hashMap.get("idSender");
                        newMessage.text = (String) hashMap.get("text");
                        newMessage.timestamp = (long) hashMap.get("timestamp");
                        conversation.getArrayMessage().add(newMessage);
                        adapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(conversation.getArrayMessage().size() - 1);
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

        }


    }

   /* private void setUpToolbar() {
        toolbar  = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);}

        LayoutInflater inflator = LayoutInflater.from(getApplicationContext());
        View view = inflator.inflate(R.layout.layout_toolbar, null  , false);

        image_in_toolbar = (ImageView) view.findViewById(R.id.image_friend);
        text_in_toolbar = (TextView) view.findViewById(R.id.text_friend);
        text_in_toolbar.setText("vaibhav");
        toolbar.addView(view);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home){
            Intent result = new Intent();
            result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            Chatctivity.this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent result = new Intent();
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    //ToDo:On Back Pressed

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSend) {
            String content = writeMessage.getText().toString();
            if (content.length() > 0) {
                Message newMassage = new Message();
                newMassage.text = content;
                newMassage.idSender = StaticConfig.UID;
                newMassage.idReceiver = roomId;
                newMassage.timestamp = System.currentTimeMillis();
                FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMassage);
            }
        }
        writeMessage.getText().clear();
    }
}

class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private Conversation conversation;
    private HashMap<String,Bitmap> bimapAvatar;
    private String userNameMessage;
    private HashMap<String,String> nameFriend;
    private HashMap<String,DatabaseReference> bitmapAvatarDB;
    private HashMap<String,DatabaseReference> NameDB;
    private Bitmap bitmapUser;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == Chatctivity.USER_MESSAGE_ID){
            LayoutInflater layoutInflater1 = LayoutInflater.from(context);
            View view1 = layoutInflater1.inflate(R.layout.rc_item_chat_user,parent,false);
            return new UserChatHolder(view1);
        }else if (viewType == Chatctivity.FRIEND_MESSAGE_ID){
            LayoutInflater layoutInflater2 = LayoutInflater.from(context);
            View view2 = layoutInflater2.inflate(R.layout.rc_item_chat_friend,parent,false);
            return new UserFriendHolder(view2);
        }
        return null;
    }

    public chatAdapter(Context context, Conversation conversation, HashMap<String, Bitmap> bimapAvatar, Bitmap bitmapUser, String userNameMessage,HashMap<String,String> nameFriend) {
        this.context = context;
        this.conversation = conversation;
        this.bimapAvatar = bimapAvatar;
        this.bitmapUser = bitmapUser;
        this.userNameMessage = userNameMessage;
        this.nameFriend = nameFriend;
        NameDB = new HashMap<>();
        bitmapAvatarDB = new HashMap<>();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (holder.getItemViewType() == Chatctivity.FRIEND_MESSAGE_ID){
            ((UserFriendHolder)holder).text_friend_message.setText(conversation.getArrayMessage().get(position).text);
            String time = new SimpleDateFormat("EEE d MM yyyy").format(new Date(conversation.getArrayMessage().get(position).timestamp));
            String today = new SimpleDateFormat("EEE d MM yyyy").format(new Date(System.currentTimeMillis()));
           if (time.equals(today)){
               ((UserFriendHolder)holder).text_friend_timestamp.setText(new SimpleDateFormat("HH:mm").format(new Date(conversation.getArrayMessage().get(position).timestamp)));
           }else{
               ((UserFriendHolder)holder).text_friend_timestamp.setText(new SimpleDateFormat("MMM d").format(new Date(conversation.getArrayMessage().get(position).timestamp)));
           }

            final String id = conversation.getArrayMessage().get(position).idSender;
            Bitmap currentAvata = bimapAvatar.get(id);
            String nameOfFriend = nameFriend.get(id);

            if (currentAvata != null ) {
                ((UserFriendHolder) holder).image_friend.setImageBitmap(currentAvata);
                ((UserFriendHolder) holder).text_friend_name.setText(nameOfFriend);
            }else{
                if (NameDB.get(id) == null){
                    NameDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/name"));
                    NameDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                           if (dataSnapshot.getValue() != null){
                               String friendName = (String) dataSnapshot.getValue();
                               Chatctivity.userReceiver.put(id, friendName);
                               notifyDataSetChanged();
                           }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if (bitmapAvatarDB.get(id) == null){
                    bitmapAvatarDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" +id + "/avata"));
                    bitmapAvatarDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null){
                                String avataStr = (String) dataSnapshot.getValue();
                                if (!avataStr.equals(StaticConfig.STR_DEFAULT_BASE64)){
                                    byte[] decodedString = Base64.decode(avataStr,Base64.DEFAULT);
                                    Bitmap avataMyFriend1 = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
                                    Chatctivity.bitmapAvataFriend.put(id,avataMyFriend1);
                                }else {
                                    Bitmap avataMyFriend2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.default_avata);
                                    Chatctivity.bitmapAvataFriend.put(id,avataMyFriend2);
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }else if(holder.getItemViewType() == Chatctivity.USER_MESSAGE_ID ){
            ((UserChatHolder)holder).text_user_message.setText(conversation.getArrayMessage().get(position).text);
            if(bitmapUser != null){
                ((UserChatHolder)holder).image_user.setImageBitmap(bitmapUser);
            }
            if (userNameMessage != null){
                ((UserChatHolder)holder).text_user_name.setText(userNameMessage);
            }
            String timeUser = new SimpleDateFormat("EEE d MM yyyy").format(new Date(conversation.getArrayMessage().get(position).timestamp));
            String todayUser = new SimpleDateFormat("EEE d MM yyyy").format(new Date(System.currentTimeMillis()));
            if (timeUser.equals(todayUser)){
                ((UserChatHolder)holder).text_user_timestamp.setText(new SimpleDateFormat("HH:mm").format(new Date(conversation.getArrayMessage().get(position).timestamp)));
            }else{
                ((UserChatHolder)holder).text_user_timestamp.setText(new SimpleDateFormat("MMM d").format(new Date(conversation.getArrayMessage().get(position).timestamp)));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        String senderId = conversation.getArrayMessage().get(position).idSender;
        if (senderId.equals(StaticConfig.UID)){
            return Chatctivity.USER_MESSAGE_ID;
        }else {
            return Chatctivity.FRIEND_MESSAGE_ID;
        }
        //return conversation.getArrayMessage().get(position).idSender.equals(StaticConfig.UID) ? Chatctivity.USER_MESSAGE_ID : Chatctivity.FRIEND_MESSAGE_ID;
    }

    @Override
    public int getItemCount() {
        return conversation.getArrayMessage().size();
    }
}

class UserChatHolder extends RecyclerView.ViewHolder{
    public TextView text_user_message, text_user_name, text_user_timestamp;
    public CircleImageView image_user;

    public UserChatHolder(View itemView) {
        super(itemView);
        text_user_message = (TextView) itemView.findViewById(R.id.textContentUser);
        image_user = (CircleImageView) itemView.findViewById(R.id.image_user_chat);
        text_user_name = (TextView) itemView.findViewById(R.id.textUserName);
        text_user_timestamp = (TextView) itemView.findViewById(R.id.userTimestamp);
    }
}

class UserFriendHolder extends RecyclerView.ViewHolder{
    public TextView text_friend_message,text_friend_name,text_friend_timestamp;
    public CircleImageView image_friend;

    public UserFriendHolder(View itemView) {
        super(itemView);
        text_friend_message = (TextView) itemView.findViewById(R.id.textContentFriend);
        image_friend = (CircleImageView) itemView.findViewById(R.id.image_user_chat);
        text_friend_name = (TextView) itemView.findViewById(R.id.textFriendName);
        text_friend_timestamp = (TextView) itemView.findViewById(R.id.friendtimestamp);
    }
}
