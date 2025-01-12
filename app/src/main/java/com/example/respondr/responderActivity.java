package com.example.respondr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class responderActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private ProgressDialog logoutProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        logoutProgressDialog = new ProgressDialog(this);
        logoutProgressDialog.setMessage("Logging out...");
        logoutProgressDialog.setCancelable(false);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configure OsmDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        } else {
            // Permissions granted on first run, set up location overlay
            setupLocationOverlay();
        }

        // Check if ID proof is uploaded after login
        checkIDProof();

        // Check user login
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check permissions again on resume
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupLocationOverlay();
        }
    }

    // Add location overlay
    private void setupLocationOverlay() {
        if (locationOverlay == null) {
            locationOverlay = new MyLocationNewOverlay(mapView);
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
            mapView.getOverlays().add(locationOverlay);
        }

        // Wait for first location fix and update the map on the main thread
        locationOverlay.runOnFirstFix(() -> {
            GeoPoint currentLocation = locationOverlay.getMyLocation();
            if (currentLocation != null) {
                // Make sure to update the map view on the main thread
                runOnUiThread(() -> {
                    mapView.getController().setCenter(currentLocation);
                    addResponderMarker(currentLocation);
                });
            }
        });
    }

    private void addResponderMarker(GeoPoint location) {
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle("Responder Location");
        mapView.getOverlays().add(marker);
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.responder_menu, menu);  // Ensure this points to the correct XML file
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            // Navigate to Profile screen
            Intent profileIntent = new Intent(this, ResponderProfileActivity.class);
            startActivity(profileIntent);
            return true;
        } else if (id == R.id.action_logout) {
            // Handle logout logic
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Show ProgressDialog
                    logoutProgressDialog.show();

                    // Simulate logout process using a handler with a delay
                    new Handler().postDelayed(() -> {
                        // Firebase sign-out
                        auth.signOut();
                        Toast.makeText(responderActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();

                        // Dismiss ProgressDialog
                        logoutProgressDialog.dismiss();

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

    private void checkIDProof() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userDoc = firestore.collection("users").document(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String idProofUrl = documentSnapshot.getString("idProofUrl");

                // If ID proof URL is null or missing, show the dialog after 5 seconds
                if (idProofUrl == null || idProofUrl.isEmpty()) {
                    // Delay the dialog by 5 seconds
                    new Handler().postDelayed(() -> showIDProofDialog(), 5000);
                }
            }
        });
    }

    private void showIDProofDialog() {
        // Create an AlertDialog asking the user to upload ID proof
        new AlertDialog.Builder(this)
                .setTitle("Upload ID Proof")
                .setMessage("Please upload your ID proof to complete your profile.")
                .setCancelable(false)
                .setPositiveButton("Edit Profile", (dialog, which) -> {
                    // Navigate to profile edit activity if user clicks "Edit Profile"
                    Intent intent = new Intent(responderActivity.this, ResponderProfileEditActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cleanup location overlay when activity is paused
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            mapView.getOverlays().remove(locationOverlay);
        }
    }
}
