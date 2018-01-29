package com.example.webczar.chat_navigate.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.webczar.chat_navigate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class signUp_frag extends Fragment {


    public OnSignUpClickListener listener;

    public interface OnSignUpClickListener{
        public void OnSignClick(View v);
        public void OnEditSignEmail(String email);
        public void OnEditSignPass(String pass, String conpassword);
        public void OnEditSignName(String name);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnSignUpClickListener)context;
    }

    public static final String TITLE = "Sign Up";
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private TextInputEditText email_input,pass_input,conpass_input,name_input;
    private TextInputLayout layoutEmailSignin,layoutPassSignin,layoutConPAssSignin,layoutNameSignin;
    private Button signUp;

    public signUp_frag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_up_frag, container, false);

        email_input = (TextInputEditText) view.findViewById(R.id.edit_email_signup);
        pass_input = (TextInputEditText) view.findViewById(R.id.edit_pass_signup);
        conpass_input = (TextInputEditText) view.findViewById(R.id.edit_conpass_signup);
        name_input = (TextInputEditText) view.findViewById(R.id.edit_name_signup);
        layoutNameSignin = (TextInputLayout) view.findViewById(R.id.textInputLayoutNameSignIn);
        layoutEmailSignin = (TextInputLayout) view.findViewById(R.id.textInputLayoutEmailSignIn);
        layoutPassSignin = (TextInputLayout) view.findViewById(R.id.textInputLayoutPassSignIn);
        layoutConPAssSignin = (TextInputLayout) view.findViewById(R.id.textInputLayoutConSignIn);
        signUp = (Button) view.findViewById(R.id.btn_signUp);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = email_input.getText().toString().trim();
                String passInput = pass_input.getText().toString().trim();
                String conPassInput = conpass_input.getText().toString().trim();
                String nameInput = name_input.getText().toString();

                if (TextUtils.isEmpty(nameInput)) {
                    layoutNameSignin.setError(getString(R.string.err_msg_name));
                    return;
                } else if (!TextUtils.isEmpty(nameInput)) {
                    validateUserName(nameInput);
                } else {
                    layoutNameSignin.setErrorEnabled(false);
                }

                 if (TextUtils.isEmpty(emailInput)) {
            layoutEmailSignin.setError(getString(R.string.err_msg_email));
            return;
        }else if (!validate(emailInput)){
            layoutEmailSignin.setError(getString(R.string.err_msg_valid_email));
            return;
        }else {layoutEmailSignin.setErrorEnabled(false);}

        if (TextUtils.isEmpty(passInput)) {
            layoutPassSignin.setError(getString(R.string.err_msg_pass));
            return;
        }else{layoutPassSignin.setErrorEnabled(false);}

        if (!TextUtils.equals(conPassInput,passInput)){
            layoutConPAssSignin.setError(getString(R.string.err_msg_conpass));
        }else {layoutConPAssSignin.setErrorEnabled(false);}
                listener.OnEditSignEmail(emailInput);
                listener.OnEditSignName(nameInput);
                listener.OnEditSignPass(passInput,conPassInput);
                listener.OnSignClick(v);
            }
        });
        return view;
    }

    private void validateUserName(final String nameInput) {

        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = ds.child("name").getValue(String.class);
                    if (name == nameInput){
                        layoutNameSignin.setError(getString(R.string.err_msg_name_validate));
                    }else {
                        layoutNameSignin.setErrorEnabled(false);
                    }
                }
              }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

}
