package com.example.webczar.chat_navigate.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class Login_frag extends Fragment {

    public OnLoginClickListener listener;
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public interface OnLoginClickListener {
        public void OnloginClick(View v);

        public void OnresetClick(View v);

        public void OnEditEmail(String email);

        public void OnEditPass(String pass);
    }

    private TextInputEditText email_input, pass_input;
    private Button logIn, forgotPass;
    //private FirebaseAuth firebaseAuth;
    //private ProgressBar progressBar;
    private TextInputLayout layoutEmailLogin, layoutPassLogin;

    public static final String TITLE = "Sign In";

    public Login_frag() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnLoginClickListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_frag, container, false);
        layoutEmailLogin = (TextInputLayout) view.findViewById(R.id.LayoutEmailLogin);
        layoutPassLogin = (TextInputLayout) view.findViewById(R.id.LayoutPassLogin);

        logIn = (Button) view.findViewById(R.id.btn_login);
        forgotPass = (Button) view.findViewById(R.id.btn_reset_login);
        email_input = (TextInputEditText) view.findViewById(R.id.btn_email_login);
        pass_input = (TextInputEditText) view.findViewById(R.id.btn_pass_login);


        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_input.getText().toString();
                final String password = pass_input.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    layoutEmailLogin.setError(getString(R.string.err_msg_email));
                    return;
                } else if (!validate(email)) {
                    layoutEmailLogin.setError(getString(R.string.err_msg_valid_email));
                    return;
                } else {
                    layoutEmailLogin.setErrorEnabled(false);
                }

                if (TextUtils.isEmpty(password)) {
                    layoutPassLogin.setError(getString(R.string.err_msg_pass));
                    return;
                } else {
                    layoutPassLogin.setErrorEnabled(false);
                }

                listener.OnEditEmail(email);
                listener.OnEditPass(password);
                listener.OnloginClick(v);
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getContext(),ResetActivity.class));
                listener.OnresetClick(v);
            }
        });

        return view;
    }

    private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

}


