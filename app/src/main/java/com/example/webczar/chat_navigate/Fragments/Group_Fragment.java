package com.example.webczar.chat_navigate.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webczar.chat_navigate.Activities.AddGroupActivity;
import com.example.webczar.chat_navigate.Activities.Chatctivity;
import com.example.webczar.chat_navigate.DataBase.FriendDB;
import com.example.webczar.chat_navigate.DataBase.GroupDB;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.MainActivity;
import com.example.webczar.chat_navigate.R;
import com.example.webczar.chat_navigate.Structure.Group;
import com.example.webczar.chat_navigate.Structure.ListFriend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;


public class Group_Fragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos";
    private RecyclerView recyclerView_group;
    private ArrayList<Group> listGroup;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference reference;
    private GroupAdapter adapter;
    private ProgressBar progressBar;
    public static final int CONTEXT_MENU_DELETE = 1;
    private FloatingActionButton floatingActionButton;
    public static final int CONTEXT_MENU_EDIT = 2;
    public static final int CONTEXT_MENU_LEAVE = 3;
    public static final int REQUEST_EDIT_GROUP = 0;


    public Group_Fragment() {
        // Required empty public constructor
    }

    /*public static Group_Fragment newInstance(String param1, String param2) {
        Group_Fragment fragment = new Group_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_group_, container, false);
        recyclerView_group = (RecyclerView) view.findViewById(R.id.recycler_group);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_group);
        swipeRefreshLayout.setOnRefreshListener(this);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab_group);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_group);
        listGroup = GroupDB.getInstance(getContext()).getListGroups();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView_group.setLayoutManager(gridLayoutManager);
        adapter = new GroupAdapter(listGroup,getContext());
        recyclerView_group.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab_group);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),AddGroupActivity.class));
            }
        });

        reference = FirebaseDatabase.getInstance().getReference();
        if (listGroup.size() == 0) {
            swipeRefreshLayout.setRefreshing(true);
            getListGroup();
        }
        return view;
    }


    private void getListGroup() {
        reference.child("user/" + StaticConfig.UID + "/group").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapListGroup = (HashMap) dataSnapshot.getValue();
                    Iterator iterator = mapListGroup.keySet().iterator();
                    while (iterator.hasNext()) {
                        String idGroup = (String) mapListGroup.get(iterator.next().toString());
                        Group newGroup = new Group();
                        newGroup.id = idGroup;
                        listGroup.add(newGroup);
                    }
                    getGroupInfo(0);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(MainActivity.class.getSimpleName(), String.valueOf(databaseError));
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getGroupInfo(final int groupIndex) {
        //means if list is empty
        if (groupIndex == listGroup.size()) {
            swipeRefreshLayout.setRefreshing(false);
            adapter.notifyDataSetChanged();
        } else {
            reference.child("group/" + listGroup.get(groupIndex).id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapGroup = (HashMap) dataSnapshot.getValue();
                        ArrayList<String> member = (ArrayList<String>) mapGroup.get("member");
                        HashMap mapGroupInfo = (HashMap) mapGroup.get("groupInfo");
                        for (String idMembers : member) {
                            //add id  listGroup
                            listGroup.get(groupIndex).member.add(idMembers);
                        }
                        listGroup.get(groupIndex).groupInfo.put("name", (String) mapGroupInfo.get("name"));
                        listGroup.get(groupIndex).groupInfo.put("admin", (String) mapGroupInfo.get("admin"));
                    }
                    GroupDB.getInstance(getContext()).addGroup(listGroup.get(groupIndex));
                    Log.d("Group_Fragment", listGroup.get(groupIndex).id + ": " + dataSnapshot.toString());
                    getGroupInfo(groupIndex + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Group_Fragment", String.valueOf(databaseError));
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        listGroup.clear();
        GroupAdapter.listFriend = null;
        GroupDB.getInstance(getContext()).dropDB();
        adapter.notifyDataSetChanged();
        getListGroup();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_GROUP && resultCode == Activity.RESULT_OK){
            GroupDB.getInstance(getContext()).dropDB();
            listGroup.clear();
            GroupAdapter.listFriend = null;
            getListGroup();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId){
            case CONTEXT_MENU_DELETE:
                int posGroup = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(posGroup).groupInfo.get("admin")).equals(StaticConfig.UID)) {
                    Group group = listGroup.get(posGroup);
                    listGroup.remove(posGroup);
                    if(group != null){
                        progressBar.setVisibility(View.VISIBLE);
                        deleteGroup(group, 0);
                    }
                }else{
                    Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
                }
                break;
            case CONTEXT_MENU_EDIT:
                int posGroup1 = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(posGroup1).groupInfo.get("admin")).equals(StaticConfig.UID)) {
                    Intent intent = new Intent(getContext(), AddGroupActivity.class);
                    intent.putExtra("groupId", listGroup.get(posGroup1).id);
                    startActivityForResult(intent, REQUEST_EDIT_GROUP);
                }else{
                    Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
                }

                break;

            case CONTEXT_MENU_LEAVE:
                int position = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(position).groupInfo.get("admin")).equals(StaticConfig.UID)) {
                    Toast.makeText(getActivity(), "Admin cannot leave group", Toast.LENGTH_LONG).show();
                }else{
                    //waitingLeavingGroup.show();
                    Group groupLeaving = listGroup.get(position);
                    progressBar.setVisibility(View.VISIBLE);
                    leaveGroup(groupLeaving);
                }
                break;        }


        return super.onContextItemSelected(item);



    }

    private void leaveGroup(final Group group) {
        //get referance of member if member equals to uid
        reference.child("group/" + group.id + "/member").orderByValue().equalTo(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"Error while Leaving the group", Toast.LENGTH_LONG);
                }else {
                    String memberIndex = "";
                    ArrayList<String> result = (ArrayList<String>) dataSnapshot.getValue();
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i) != null) {
                            memberIndex = String.valueOf(i);
                        }
                    }

                    //check for uid in group and remove that value
                    reference.child("user/" + StaticConfig.UID).child("group/" + group.id).removeValue();
                    //removr the value of member
                    reference.child("group/" + group.id + "member/").child(memberIndex).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //remove id from list and from database
                            GroupDB.getInstance(getContext()).deleteGroup(group.id);
                            listGroup.remove(group);
                            //progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(),"You have successfuly left the group", Toast.LENGTH_SHORT);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error occured while leaving the group", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getContext(), "Error occured while leaving the group", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void deleteGroup(final Group group, final int index) {
        //check if there are any members in the group
        if(index == group.member.size()){
            //now remove the group
            reference.child("group"+group.id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                    GroupDB.getInstance(getContext()).deleteGroup(group.id);
                    listGroup.remove(index);
                    adapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),"Can't delete this group right now", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            reference.child("user/"+ group.member.get(index) + "/group" + group.id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    deleteGroup(group, index+1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"Can not connect Server", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Group> listGroup;
    public static ListFriend listFriend = null;
    private Context context;

    public GroupAdapter(ArrayList<Group> listGroup, Context context) {
        this.listGroup = listGroup;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rc_item_group, parent, false);
        return new GroupHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        //get name from listGroup
        final String groupName = listGroup.get(position).groupInfo.get("name");
        if (groupName != null && groupName.length() > 0) {
            //set text to text Group name
            ((GroupHolder) holder).txtGroupName.setText(groupName);
            ((GroupHolder) holder).iconGroup.setText((groupName.charAt(0) + "").toUpperCase());
        }
        ((GroupHolder) holder).btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setTag(new Object[]{groupName, position});
                view.getParent().showContextMenuForChild(view);
            }
        });

        ((View) ((GroupHolder) holder).txtGroupName.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listFriend == null) {
                    listFriend = FriendDB.getInstance(context).getListFriend();
                }
                Intent intent = new Intent(context, Chatctivity.class);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, groupName);
                ArrayList<CharSequence> idFriend = new ArrayList<>();
                Chatctivity.bitmapAvataFriend = new HashMap<>();
                for (String id : listGroup.get(position).member) {
                    idFriend.add(id);
                    String avata = listFriend.getAvataById(id);
                    if (!avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                        byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                        Chatctivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                    } else if (avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                        Chatctivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                    } else {
                        Chatctivity.bitmapAvataFriend.put(id, null);
                    }
                }
                intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, listGroup.get(position).id);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return listGroup.size(); }
}


class GroupHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    public TextView iconGroup, txtGroupName;
    public ImageButton btnMore;

    public GroupHolder(View itemView) {
        super(itemView);
        itemView.setOnCreateContextMenuListener(this);
        iconGroup = (TextView) itemView.findViewById(R.id.icon_group);
        txtGroupName = (TextView) itemView.findViewById(R.id.txtName);
        btnMore = (ImageButton) itemView.findViewById(R.id.btnMoreAction);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle((String) ((Object[]) btnMore.getTag())[0]);
        Intent data = new Intent();
        data.putExtra(Group_Fragment.CONTEXT_MENU_KEY_INTENT_DATA_POS, (Integer) ((Object[]) btnMore.getTag())[1]);
        menu.add(Menu.NONE, Group_Fragment.CONTEXT_MENU_EDIT, Menu.NONE, "Edit group").setIntent(data);
        menu.add(Menu.NONE, Group_Fragment.CONTEXT_MENU_DELETE, Menu.NONE, "Delete group").setIntent(data);
        menu.add(Menu.NONE, Group_Fragment.CONTEXT_MENU_LEAVE, Menu.NONE, "Leave group").setIntent(data);
    }
}