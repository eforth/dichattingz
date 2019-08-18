package com.vectorjm.dichattinz;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Locale;

public class RegisterFragment extends Fragment {

    static String TAG = "RegisterFragment";

    private AppCompatImageButton backBtn;
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText emailText;
    private EditText passwordText;
    private ProgressBar progressBar;
    private Button registerBtn;

    private FirebaseAuth auth;

    public RegisterFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        firstNameText = view.findViewById(R.id.firstName);
        lastNameText = view.findViewById(R.id.lastName);
        emailText = view.findViewById(R.id.email);
        passwordText = view.findViewById(R.id.password);
        progressBar = view.findViewById(R.id.progressBar);

        backBtn = view.findViewById(R.id.backBtn);
        registerBtn = view.findViewById(R.id.registerBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.baseContainer, new LoginFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser();
            }
        });
    }

    private boolean validateForm() {

        if (TextUtils.isEmpty(firstNameText.getText().toString())) return false;
        if (TextUtils.isEmpty(lastNameText.getText().toString())) return false;
        if (TextUtils.isEmpty(emailText.getText().toString())) return false;
        if (TextUtils.isEmpty(passwordText.getText().toString())) return false;

        return true;
    }

    private void registerNewUser() {

        progressBar.setVisibility(View.VISIBLE);

        if (!validateForm()) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(), "Register form is not valid", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Log.w(TAG, task.getException());
                            Toast.makeText(getContext(), "Unable to register new user",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String name = String.format(Locale.getDefault(), " %s %s",
                                firstNameText.getText().toString(),
                                lastNameText.getText().toString()
                        );

                        FirebaseUser user = auth.getCurrentUser();
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                progressBar.setVisibility(View.INVISIBLE);

                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.baseContainer, new MainFragment())
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }
                });
    }
}
