package com.example.respondr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.respondr.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private TextView headerUserEmail,headerUserName;
    private String displayName;
    private ProgressDialog logoutprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        logoutprogress = new ProgressDialog(MainActivity.this);
        logoutprogress.setMessage("Logging out...");
        logoutprogress.setCancelable(false);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return; // Prevent further execution
        }

        // Check the role from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "Public"); // Default to "Public"

        // Redirect based on role
        if ("Responder".equals(userRole)) {
            Intent intent = new Intent(MainActivity.this, responderActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close MainActivity
            return; // Prevent further execution
        }

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);
        headerUserEmail = headerView.findViewById(R.id.userEmailTextView);
        headerUserEmail.setText(currentUser.getEmail());
        headerUserName = headerView.findViewById(R.id.userNameTextView);
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (displayName == null || displayName.isEmpty()) {
                displayName = email != null ? email.split("@")[0] : "Guest"; // Fallback
            }
            headerUserName.setText(displayName);
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.inbox, R.id.settings)
                .setOpenableLayout(drawer)
                .build();

        // Setup Navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.logout_button) {
                    logout();
                } else{
                    NavigationUI.onNavDestinationSelected(item, navController);
                }
                drawer.closeDrawer(binding.navView);
                return true;
            }

        });
    }

    // Perform Logout with ProgressDialog
    private void logout() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Show ProgressDialog
                    logoutprogress.show();

                    // Simulate logout process using a handler with a delay
                    new Handler().postDelayed(() -> {
                        // Firebase sign-out
                        auth.signOut();
                        Toast.makeText(MainActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();

                        // Dismiss ProgressDialog
                        logoutprogress.dismiss();

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
        Intent intent = new Intent(MainActivity.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}