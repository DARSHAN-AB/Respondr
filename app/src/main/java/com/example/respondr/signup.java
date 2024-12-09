package com.example.respondr;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
    CheckBox checkbox;
    private boolean isPasswordVisible = false;

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
        checkbox = findViewById(R.id.terms_checkbox);

        float textSizeInDp = 14f;
        float scale = getResources().getDisplayMetrics().density;
        float textSizeInPx = textSizeInDp * scale;

        signupPassword.setLetterSpacing(0.4f);
        signupPassword.setTextSize(textSizeInPx / getResources().getDisplayMetrics().scaledDensity); // Set consistent text size
        signupPassword.setTypeface(Typeface.DEFAULT);

        signupPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Detect if the right drawable is clicked
                if (event.getRawX() >= (signupPassword.getRight() - signupPassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (isPasswordVisible) {
                        // Switch to hidden password
                        signupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        signupPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Switch to visible password
                        signupPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        signupPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_24, 0);
                    }

                    signupPassword.setTextSize(textSizeInPx / getResources().getDisplayMetrics().scaledDensity); // Set consistent text size
                    signupPassword.setLetterSpacing(0.4f);
                    signupPassword.setTypeface(Typeface.DEFAULT);
                    isPasswordVisible = !isPasswordVisible;
                    signupPassword.setSelection(signupPassword.getText().length());

                    return true;
                }
            }
            return false;
        });

        signupConfirmPassword.setLetterSpacing(0.4f);
        signupConfirmPassword.setTextSize(textSizeInPx / getResources().getDisplayMetrics().scaledDensity); // Set consistent text size
        signupConfirmPassword.setTypeface(Typeface.DEFAULT);

        signupConfirmPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Detect if the right drawable is clicked
                if (event.getRawX() >= (signupConfirmPassword.getRight() - signupConfirmPassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (isPasswordVisible) {
                        // Switch to hidden password
                        signupConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        signupConfirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Switch to visible password
                        signupConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        signupConfirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_24, 0);
                    }

                    signupConfirmPassword.setTextSize(textSizeInPx / getResources().getDisplayMetrics().scaledDensity); // Set consistent text size
                    signupConfirmPassword.setLetterSpacing(0.4f);
                    signupConfirmPassword.setTypeface(Typeface.DEFAULT);
                    isPasswordVisible = !isPasswordVisible;
                    signupConfirmPassword.setSelection(signupPassword.getText().length());

                    return true;
                }
            }
            return false;
        });

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

                if (!checkbox.isChecked()) {
                    // Show a message if the checkbox is not checked
                    Toast.makeText(signup.this,
                            "Please accept the Terms and Conditions to proceed",
                            Toast.LENGTH_SHORT).show();
                }else if(signuppassword.isEmpty()){
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