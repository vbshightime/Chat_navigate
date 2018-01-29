package com.example.webczar.chat_navigate;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.webczar.chat_navigate.Activities.LoginActivity;
import com.example.webczar.chat_navigate.Activities.ResetActivity;
import com.example.webczar.chat_navigate.DataBase.FriendDB;
import com.example.webczar.chat_navigate.DataBase.GroupDB;
import com.example.webczar.chat_navigate.Fragments.Friends_fragment;
import com.example.webczar.chat_navigate.Fragments.Group_Fragment;
import com.example.webczar.chat_navigate.Fragments.Library;
import com.example.webczar.chat_navigate.Fragments.Profile_fragment;
import com.example.webczar.chat_navigate.Helper.SharedPreferanceHelper;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.Services.ServiceUtils;
import com.example.webczar.chat_navigate.Structure.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity  {

    private Toolbar toolbar;
    public OnFabClickListener clickListener;
    private FragmentManager fragmentManager;
    private android.support.v4.app.Fragment fragment;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    public static FloatingActionButton floatingActionButton;
    private DatabaseReference userDB;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth.AuthStateListener listener;

    public interface OnFabClickListener{
        public void onclickFab(View v);
    }



        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();
        setUpdrawer();
            initFirebase();
            userDB = FirebaseDatabase.getInstance().getReference().child("user");


        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new Friends_fragment();
        fragmentTransaction.replace(R.id.content_frame,fragment);
        fragmentTransaction.commit();



        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                int itemId = item.getItemId();
                if (itemId == R.id.friend){
                    fragment =new Friends_fragment();
                    toolbar.setTitle(getString(R.string.friend_label));
                }else if (itemId == R.id.profile){
                    fragment =new Profile_fragment();
                    toolbar.setTitle(getString(R.string.profile_label));
                }else if (itemId == R.id.group){
                    fragment =new Group_Fragment();
                    toolbar.setTitle(getString(R.string.group_label));
                }else if (itemId == R.id.library){
                    fragment =new Library();
                    toolbar.setTitle(getString(R.string.library_label));}
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.commit();

                drawerLayout =(DrawerLayout) findViewById(R.id.drawer_layout);
                assert drawerLayout!= null;
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = mAuth.getCurrentUser();
                if (user!=null){
                    StaticConfig.UID = user.getUid();
                }else {
                    MainActivity.this.finish();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    Log.d(TAG,"onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpdrawer(){
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
        ServiceUtils.stopTheService(getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            mAuth.removeAuthStateListener(listener);
        }
    }

    @Override
    protected void onDestroy() {
        ServiceUtils.startTheService(getApplicationContext());
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_settings,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

         int itemId = item.getItemId();
        if(itemId == R.id.set_signOut_id){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Sign Out")
                    .setMessage("Are You Sure!!")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            signOut();

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        if (itemId == R.id.set_reset_id){
            resetPass();
        }
        return true;
    }

    private void resetPass() {

        startActivity(new Intent(MainActivity.this, ResetActivity.class));
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        FriendDB.getInstance(getApplicationContext()).dropDB();
        GroupDB.getInstance(getApplicationContext()).dropDB();
        ServiceUtils.stopTheService(getApplicationContext());
        MainActivity.this.finish();
    }

    /*class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder>{

        private Context context;
        private String[] dataList;
        private android.support.v4.app.Fragment fragment;
        private FragmentManager fragmentManager;

        public DrawerAdapter(Context context, String[] dataList) {
            this.context = context;
            this.dataList = dataList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.rc_item_drawer, null, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DrawerAdapter.ViewHolder holder, final int position) {
            ((ViewHolder) holder).drawerTitles.setText(dataList[position]);
            fragmentManager = getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new Friends_fragment();
            fragmentTransaction.replace(R.id.content_frame,fragment);
            fragmentTransaction.commit();
            ((ViewHolder) holder).drawerTitles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if (position == 0){
                       fragment = new Friends_fragment();
                   }else if (position == 1){
                       fragment = new Profile_fragment();
                   }else if (position == 2){
                       fragment = new Group_Fragment();
                   }else if (position == 3){
                       fragment = new Library();
                   }
                }
            });
        }*/


        /*@Override
        public int getItemCount() {
            return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView drawerTitles;
            public ViewHolder(View itemView) {
                super(itemView);
                drawerTitles = (TextView) itemView.findViewById(R.id.text_drawer_titles);
            }
        }*/
    }


