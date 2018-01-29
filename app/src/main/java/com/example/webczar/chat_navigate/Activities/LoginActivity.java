package com.example.webczar.chat_navigate.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.webczar.chat_navigate.Adapters.ViewPagerAdapter;
import com.example.webczar.chat_navigate.Fragments.Login_frag;
import com.example.webczar.chat_navigate.Fragments.signUp_frag;
import com.example.webczar.chat_navigate.Helper.SharedPreferanceHelper;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.MainActivity;
import com.example.webczar.chat_navigate.R;
import com.example.webczar.chat_navigate.Structure.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements Login_frag.OnLoginClickListener, signUp_frag.OnSignUpClickListener{

    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FirebaseAuth firebaseAuth;
    private String dataEmail;
    private String dataPass;
    private String dataSignEmail;
    private String dataSignPass;
    private String dataSignName;
    private String dataSignConPass;
    private AuthUtils authUtils;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private boolean firstTimeAccess;


    private static final String TAG = LoginActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        progressBar.setVisibility(View.GONE);

        setViewPager();
        firstTimeAccess =true;
        initFirebase();
    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void initFirebase() {
        firebaseDatabase =FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        authUtils = new AuthUtils();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    StaticConfig.UID = firebaseUser.getUid();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + StaticConfig.UID);
                    if (firstTimeAccess){
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            LoginActivity.this.finish();
                        } else {
                        Log.d(TAG, "onAuthStateChanged:signed_out:");
                    }
                    firstTimeAccess = false;
                }
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void setViewPager() {
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public void OnloginClick(View v) {

        Log.e(TAG,"No Response from Login Click");

           authUtils.signIn(dataEmail,dataPass);


    }

    /*private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }*/

    @Override
    public void OnresetClick(View v) {
        startActivity(new Intent(LoginActivity.this,ResetActivity.class));
        Log.e(TAG,"No Response from Reset Click");
    }

    @Override
    public void OnEditEmail(String email) {
        this.dataEmail = email;
        Log.e(TAG,"can't get the value email");
    }

    @Override
    public void OnEditPass(String pass) {
        this.dataPass = pass;
        Log.e(TAG,"can't get the value pass");
    }

    @Override
    public void OnEditSignEmail(String email) {
        this.dataSignEmail = email;

    }

    @Override
    public void OnEditSignPass(String pass, String conpassword) {
        this.dataSignPass = pass;
        this.dataSignConPass = conpassword;
    }

    @Override
    public void OnEditSignName(String name) {
        this.dataSignName = name;
    }

    @Override
    public void OnSignClick(View v) {
        authUtils.createUser(dataSignEmail,dataSignPass);
    }


    class AuthUtils{

        void signIn(String email,String password){

            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                    Toast.makeText(LoginActivity.this,"signInWithEmail:onComplete:" + task.isSuccessful(),Toast.LENGTH_SHORT);
                    if (!task.isSuccessful()) {

                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                        // there was an error
                        Toast.makeText(getApplicationContext(), getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                    } else {
                        saveUserInfo();
                        progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            LoginActivity.this.finish();

                            Toast.makeText(LoginActivity.this, "Email is not varified!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);

                    Log.d(TAG, String.valueOf(e));
                }
            });
        }

        private void saveUserInfo() {
            databaseReference.child("user/"+StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap hashUSer = (HashMap) dataSnapshot.getValue();
                    User user = new User();
                    user.name = (String) hashUSer.get("name");
                    user.email = (String) hashUSer.get("email");
                    user.avata = (String) hashUSer.get("avata");
                    SharedPreferanceHelper.getInstance(LoginActivity.this).saveUserInfo(user);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        void createUser(String email, String password){

            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG,"createUserWithEmail:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Log.d(TAG,"Authentication failed." + task.getException());

                    } else{
                        firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser.isEmailVerified() == false){

                            firebaseUser.sendEmailVerification().addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(LoginActivity.this,
                                                "Varification mail sent to"+ firebaseUser.getEmail()+", Please varify your Email and login to webczar",
                                                Toast.LENGTH_LONG).show();
                                    }else {
                                        Log.d(TAG, "sendEmailVarification",task.getException());
                                        Toast.makeText(LoginActivity.this,"Failed to Varify", Toast.LENGTH_SHORT);
                                    }
                                }
                            }).addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this,"Please Enter valid Email", Toast.LENGTH_SHORT);
                                    Log.d(TAG, "sendEmailVarification",e);
                                }
                            });
                            initNewUserInfo();
                            Toast.makeText(LoginActivity.this, "Register and Login success", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG,String.valueOf(e));
                    Toast.makeText(LoginActivity.this,getString(R.string.connection) , Toast.LENGTH_LONG).show();
                }
            });
        }

        private void initNewUserInfo() {

                User newUser = new User();
                newUser.email = firebaseUser.getEmail();
                newUser.name = dataSignName;
                newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
                databaseReference.child("user/" + firebaseUser.getUid()).setValue(newUser);
                databaseReference.child("user").child(firebaseUser.getUid()).child("name").setValue(newUser.name);
                Log.d(TAG,"info not updated");


        }
    }

}
