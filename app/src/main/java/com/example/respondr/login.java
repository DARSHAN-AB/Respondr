package com.example.respondr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class login extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private static final int RC_SIGN_IN = 631;
    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirect;
    private boolean isPasswordVisible = false;
    private ImageButton googlelogin;


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
        googlelogin = findViewById(R.id.google);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        ProgressDialog progressL = new ProgressDialog(login.this);
        progressL.setMessage("Logging in...");
        progressL.setCancelable(false);

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
                progressL.dismiss();

                if (!loginemail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(loginemail).matches()) {
                    if (!loginpassword.isEmpty()) {
                        progressL.show();
                        auth.signInWithEmailAndPassword(loginemail, loginpassword)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        progressL.dismiss();
                                        Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(login.this, MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressL.dismiss();
                                        Toast.makeText(login.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                        loginPassword.setError("Invalid username or password");
                                        loginPassword.requestFocus();
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

        googlelogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerVibration();
                showProgressDialog();
                gsc.signOut().addOnCompleteListener(task -> {
                    signIn();
                });
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void signIn() {
        Intent googleIntent = gsc.getSignInIntent();
        startActivityForResult(googleIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                    // User closed the dialog
                    Toast.makeText(this, "Google sign-in cancelled.", Toast.LENGTH_LONG).show();
                } else {
                    // Other errors
                    Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        }

    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String displayName = user.getDisplayName();

                                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", displayName); // Save the username
                                editor.apply();

                                Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(login.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(login.this, "Something went wrong, Try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //method for triggering vibration
    private void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100); // Vibrate for 100 milliseconds
        } else {
            Toast.makeText(this, "signing in...", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show progress dialog
    private void showProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false); // Prevent user from dismissing the dialog
        progressDialog.show();
    }

}