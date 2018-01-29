package com.example.webczar.chat_navigate.Fragments;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webczar.chat_navigate.Helper.SharedPreferanceHelper;
import com.example.webczar.chat_navigate.Helper.StaticConfig;
import com.example.webczar.chat_navigate.MainActivity;
import com.example.webczar.chat_navigate.R;
import com.example.webczar.chat_navigate.Structure.Configuration;
import com.example.webczar.chat_navigate.Structure.User;
import com.example.webczar.chat_navigate.Utils.ImageUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.widget.Toast.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class Profile_fragment extends Fragment {


    private List<Configuration> listconfig = new ArrayList<>();
    private Configuration userNameConfig, fullNameConfig, DOBConfig, EmailConfig;
    private ProfileAdapter profileAdapter;
    private static final String USERNAME_LABEL = "Username";
    private static final String FULLNAME_LABEL = "fullName";
    private static final String EMAIL_LABEL = "Email";
    private static final String DOB_LABEL = "Wish me on";
    private static final String SIGNOUT_LABEL = "Sign out";
    private static final String RESETPASS_LABEL = "Change Password";
    private TextView textUser;
    private ImageView profile_pic;
    private User user;
    private Context context;
    private DatabaseReference userDB;
    private RecyclerView recyclerView;
    private DatePickerDialog datePicker;

    private static final int PICK_IMAGE = 1994;




    public Profile_fragment() {
        // Required empty public constructor
    }

    private ValueEventListener userListner = new ValueEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            listconfig.clear();
            user = dataSnapshot.getValue(User.class);
            setUserValues(user);

            if (profileAdapter != null){
                profileAdapter.notifyDataSetChanged();
            }


            if (textUser != null) {
                textUser.setText(user.name);
            }

            setProfilePic(context, user.avata);


            SharedPreferanceHelper preference = SharedPreferanceHelper.getInstance(context);
            preference.saveUserInfo(user);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

            Log.e(Profile_fragment.class.getName(), "loadPost:onCancelled", databaseError.toException());

        }
    };


    private void setProfilePic(Context context, String avata) {
        try {
            Resources res = getResources();
            Bitmap src;
            if (avata.equals("default")) {
                src = BitmapFactory.decodeResource(res, R.drawable.default_avata);
            } else {
                byte[] decodeString = Base64.decode(avata, Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length);
            }
            profile_pic.setImageDrawable(ImageUtils.getRoundedImage(context, src));

        } catch (Exception e) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_fragment, container, false);
        context = view.getContext();
        profile_pic = (ImageView) view.findViewById(R.id.profile_pic);
        textUser = (TextView) view.findViewById(R.id.User_name);

        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        userDB.addValueEventListener(userListner);

        SharedPreferanceHelper helper = SharedPreferanceHelper.getInstance(context);
        user = helper.getUserSavedValue();

        setUserValues(user);
        setProfilePic(context, user.avata);
        textUser.setText(user.name);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        profileAdapter= new ProfileAdapter(listconfig);
        recyclerView.setAdapter(profileAdapter);

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(context)
                        .setMessage(getString(R.string.message_label))
                        .setCancelable(false)
                        .setTitle("Profile Picture")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                            }
                        }).setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {

                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());

                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                String imageBase64 = ImageUtils.encodeBase64(liteImage);
                user.avata = imageBase64;
                final Toast toast = makeText(getContext(), "Profile pic Updating", LENGTH_LONG);
                toast.show();

                userDB.child("avata").setValue(imageBase64).addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Log.d(MainActivity.class.getSimpleName(), "Task not successful");
                                    toast.cancel();
                                    // save the details in shared preferance
                                    SharedPreferanceHelper sharedPreferanceHelper = SharedPreferanceHelper.getInstance(context);
                                    sharedPreferanceHelper.saveUserInfo(user);
                                    profile_pic.setImageDrawable(ImageUtils.getRoundedImage(context, liteImage));
                                    makeText(getContext(), "Success !", LENGTH_SHORT);

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        toast.cancel();
                        makeText(getContext(), "Failed To Update", LENGTH_SHORT);
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void setUserValues(User userValues) {

            listconfig.clear();

        userNameConfig = new Configuration(USERNAME_LABEL, userValues.name, R.drawable.ic_user_name);
        listconfig.add(userNameConfig);

        EmailConfig = new Configuration(EMAIL_LABEL, userValues.email, R.drawable.ic_user_email);
        listconfig.add(EmailConfig);

        DOBConfig = new Configuration(DOB_LABEL, userValues.dateOfBirth, R.drawable.ic_dob);
        listconfig.add(DOBConfig);

        fullNameConfig = new Configuration(FULLNAME_LABEL, userValues.userName, R.drawable.ic_full_name);
        listconfig.add(fullNameConfig);

    }


   public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileHolder>{

        private final List<Configuration> profileConfig;


        @Override
        public ProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rc_item_profile, parent, false);
            return new ProfileHolder(view);
        }

        public ProfileAdapter(List<Configuration> profileConfig) {
            this.profileConfig = profileConfig;

        }

        @Override
        public void onBindViewHolder(ProfileHolder holder, int position) {
            final Configuration config = profileConfig.get(position);

            holder.label.setText(config.getLabel());
            holder.value.setText(config.getValue());
            holder.icon.setImageResource(config.getIcon());

            ((View) holder.label.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(MainActivity.class.getSimpleName(),"onClick: false");


                    if (config == userNameConfig) {
                        Log.d(MainActivity.class.getSimpleName(), "User name Alert Dialogue Not Created");

                        LayoutInflater viewInflate = LayoutInflater.from(context);
                    view = viewInflate.inflate(R.layout.edit_user_name, null);
                    final EditText editUserName = (EditText) view.findViewById(R.id.dial_userName);
                    editUserName.setText(user.name);

                        Toast.makeText(context,"user botton",LENGTH_SHORT);

                    new AlertDialog.Builder(getActivity())
                            .setView(view)
                            .setTitle("Edit User Name")
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String newName = editUserName.getText().toString();
                                    if (!user.name.equals(newName)) {
                                        changeUserName(newName);
                                    }
                                    dialog.dismiss();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                    }

                    if(config == EmailConfig){


                    }
                    if (config == DOBConfig) {

                        Toast.makeText(context,"dob botton",LENGTH_SHORT).show();
                        Log.d(MainActivity.class.getSimpleName(),"Date picker not created");
                    final Calendar calendar = Calendar.getInstance();
                    int myear = calendar.get(Calendar.YEAR);
                    int mmonth = calendar.get(Calendar.MONTH);
                    int mdayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    Log.d(MainActivity.class.getSimpleName(), "Date of Birth  Dialogue Not Created");
                     datePicker = new DatePickerDialog(getActivity(),
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                   String formatedDate = (dayOfMonth)+"/"+(month+1)+"/"+(year);
                                    setPickedDate(formatedDate);
                                }
                            }, myear, mmonth, mdayOfMonth);
                        datePicker.show();
                    datePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            datePicker.dismiss();
                        }
                    });

                    }
                    if (config == fullNameConfig) {

                        Toast.makeText(context,"full botton",LENGTH_SHORT).show();
                    LayoutInflater viewInflate = LayoutInflater.from(context);
                    view = viewInflate.inflate(R.layout.edit_full_name, null);
                    final EditText editUserName = (EditText) view.findViewById(R.id.dial_fullName);
                    Log.d(MainActivity.class.getSimpleName(), "Full name Alert Dialogue Not Created");
                    new AlertDialog.Builder(getActivity())
                            .setView(view)
                            .setTitle("Edit User Name")
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String newFullName = editUserName.getText().toString();
                                    changeFullName(newFullName);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return profileConfig.size();
        }

        class ProfileHolder extends RecyclerView.ViewHolder  {
            public TextView label, value;
            public ImageView icon;

            public ProfileHolder(View itemView) {
                super(itemView);
                value = (TextView) itemView.findViewById(R.id.tv_label);
                label = (TextView) itemView.findViewById(R.id.tv_value);
                icon = (ImageView) itemView.findViewById(R.id.img_icon);

            }
        }


       private void changeFullName(String newFullName) {
           userDB.child("userName").setValue(newFullName);
           user.userName = newFullName;
           SharedPreferanceHelper helper = SharedPreferanceHelper.getInstance(context);
           helper.saveUserInfo(user);
           setUserValues(user);
       }

       private void setPickedDate(String dob) {

           userDB.child("birthDate").setValue(dob);
           user.dateOfBirth = dob;

           SharedPreferanceHelper preferanceHelper = SharedPreferanceHelper.getInstance(context);
           preferanceHelper.saveUserInfo(user);
           Log.d(MainActivity.class.getSimpleName(),"not set dob");
           setUserValues(user);

       }

       private void changeUserName(String newName) {
           userDB.child("name").setValue(newName);
           user.name = newName;
           SharedPreferanceHelper helper = SharedPreferanceHelper.getInstance(context);
           helper.saveUserInfo(user);
           setUserValues(user);

       }
   }
}



