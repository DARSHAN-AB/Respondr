package com.example.respondr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.MotionEvent;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirect;
    private boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirect = findViewById(R.id.signupRedirect);


        String signupText = "Don't have an account? Register";
        SpannableString spannableSignupText = new SpannableString(signupText);

// Find the start and end indices of "Login"
        int startLogin = signupText.indexOf("Register");
        int endLogin = startLogin + "Register".length();

// Apply the underline to "Login"
        UnderlineSpan underlineSpansignup = new UnderlineSpan();
        spannableSignupText.setSpan(underlineSpansignup, startLogin, endLogin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// Make "Login" clickable
        ClickableSpan clickableSpanLogin = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // When "Login" is clicked, open the login activity
                startActivity(new Intent(login.this, signup.class));
            }
        };
        spannableSignupText.setSpan(clickableSpanLogin, startLogin, endLogin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// Set the spannable text to the TextView
        signupRedirect.setText(spannableSignupText);
        signupRedirect.setMovementMethod(LinkMovementMethod.getInstance()); // Enable clickable links
        signupRedirect.setHighlightColor(Color.TRANSPARENT);

        float textSizeInDp = 14f;
        float scale = getResources().getDisplayMetrics().density;
        float textSizeInPx = textSizeInDp * scale;

        loginPassword.setLetterSpacing(0.4f);
        loginPassword.setTextSize(textSizeInPx / getResources().getDisplayMetrics().scaledDensity); // Set consistent text size
        loginPassword.setTypeface(Typeface.DEFAULT);

        loginPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Detect if the right drawable is clicked
                if (event.getRawX() >= (loginPassword.getRight() - loginPassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (isPasswordVisible) {
                        // Switch to hidden password
                        loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        loginPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0);
                    } else {
                        // Switch to visible password
                        loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        loginPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_24, 0);
                    }

                    loginPassword.setTextSize(textSizeInPx / getResources().getDisplayMetrics().scaledDensity); // Set consistent text size
                    loginPassword.setLetterSpacing(0.4f);
                    loginPassword.setTypeface(Typeface.DEFAULT);
                    isPasswordVisible = !isPasswordVisible;
                    loginPassword.setSelection(loginPassword.getText().length());

                    return true;
                }
            }
            return false;
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginemail = loginEmail.getText().toString();
                String loginpassword = loginPassword.getText().toString();

                if(!loginemail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(loginemail).matches()){
                    if(!loginpassword.isEmpty()){
                        auth.signInWithEmailAndPassword(loginemail,loginpassword)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(login.this, MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(login.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loginPassword.setError("Please enter your password");
                    }
                } else if (loginemail.isEmpty()) {
                    loginEmail.setError("Please enter your email");
                } else {
                    loginEmail.setError("Invalid email");
                }

            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}