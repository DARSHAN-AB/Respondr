package com.example.respondr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class responderActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ProgressDialog logoutprogressr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder);

        auth = FirebaseAuth.getInstance();
        Button Logoutr = findViewById(R.id.reslogout);
        logoutprogressr = new ProgressDialog(responderActivity.this);
        logoutprogressr.setMessage("Logging out...");
        logoutprogressr.setCancelable(false);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return; // Prevent further execution
        }

        Logoutr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Responder-specific logic
    }

    private void logout() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Show ProgressDialog
                    logoutprogressr.show();

                    // Simulate logout process using a handler with a delay
                    new Handler().postDelayed(() -> {
                        // Firebase sign-out
                        auth.signOut();
                        Toast.makeText(responderActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();

                        // Dismiss ProgressDialog
                        logoutprogressr.dismiss();

                        // Redirect to login
                        redirectToLogin();
                    }, 500); // 500-millisecond delay for simulation
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog if the user cancels
                    dialog.dismiss();
                })
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(responderActivity.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
