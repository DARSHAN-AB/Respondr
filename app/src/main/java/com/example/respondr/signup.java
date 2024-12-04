package com.example.respondr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class signup extends AppCompatActivity {

    private FirebaseAuth auth;
    EditText signupEmail, signupPassword, signupConfirmPassword;
    private Button signupButton;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupConfirmPassword = findViewById(R.id.signup_confirm_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirect = findViewById(R.id.loginRedirect);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String signupemail = signupEmail.getText().toString().trim();
                String signuppassword = signupPassword.getText().toString().trim();
                String signupconfirmpassword = signupConfirmPassword.getText().toString().trim();

                if(signupemail.isEmpty()){
                    signupEmail.setError("Please enter your email");
                    signupEmail.requestFocus();
                }

                if(signuppassword.isEmpty()){
                    signupPassword.setError("Please enter your password");
                    signupPassword.requestFocus();
                } else if(signuppassword.equals(signupconfirmpassword)) {
                    auth.createUserWithEmailAndPassword(signupemail, signuppassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(signup.this, "Signup Successfull", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(signup.this, login.class));
                            } else {
                                Toast.makeText(signup.this, "Signup Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    signupConfirmPassword.setError("Passwords do not match");
                    signupConfirmPassword.requestFocus();
                }

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(signup.this, login.class));
            }
        });

    }
}