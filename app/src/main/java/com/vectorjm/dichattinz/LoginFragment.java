package com.vectorjm.dichattinz;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
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

public class LoginFragment extends Fragment {

    private FirebaseAuth auth;
    private EditText username, password;
    private ProgressBar progressBar;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.createAccountBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.baseContainer, new RegisterFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        auth = FirebaseAuth.getInstance();
        username = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        progressBar = view.findViewById(R.id.progressBar);

        view.findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate();
            }
        });
    }

    private void authenticate() {

        String usernameTxt = username.getText().toString();
        String passwordTxt = password.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(usernameTxt) || TextUtils.isEmpty(passwordTxt)) {
            Toast.makeText(getContext(), "Please check Username/Password fields.",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        auth.signInWithEmailAndPassword(usernameTxt, passwordTxt)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.INVISIBLE);

                        if (task.isSuccessful()) {

                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.baseContainer, new MainFragment())
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            Toast.makeText(getContext(), "Username/Password is incorrect",
                                    Toast.LENGTH_SHORT).show();
                            username.setText("");
                            password.setText("");
                        }

                    }
                });
    }
}
