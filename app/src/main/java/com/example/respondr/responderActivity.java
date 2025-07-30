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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class responderActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private ProgressDialog logoutProgressDialog;
    private FloatingActionButton fabRecenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder);

        // Initialize Firebase
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
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use Mapnik for a clean look
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true); // Enable zoom controls
        mapView.setMinZoomLevel(10.0); // Prevent zooming out too far
        mapView.setMaxZoomLevel(20.0); // Prevent zooming in too close
        mapView.getController().setZoom(15.0);

        // Add Compass Overlay
        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Initialize FAB for recentering
        fabRecenter = findViewById(R.id.fab_recenter);
        fabRecenter.setOnClickListener(v -> recenterMap());

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        } else {
            setupLocationOverlay();
        }

        // Check if ID proof is uploaded
        checkIDProof();

        // Check user login
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
        }

        // Fade-in animation for map
        mapView.setAlpha(0f);
        mapView.animate().alpha(1f).setDuration(1000).start();
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

    // Set up location overlay
    private void setupLocationOverlay() {
        if (locationOverlay == null) {
            locationOverlay = new MyLocationNewOverlay(mapView);
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
            mapView.getOverlays().add(locationOverlay);
        }

        // Wait for first location fix and update the map
        locationOverlay.runOnFirstFix(() -> {
            GeoPoint currentLocation = locationOverlay.getMyLocation();
            if (currentLocation != null) {
                runOnUiThread(() -> {
                    mapView.getController().setCenter(currentLocation);
                    addResponderMarker(currentLocation);
                });
            }
        });
    }

    // Add custom responder marker
    private void addResponderMarker(GeoPoint location) {
        // Remove existing markers to avoid duplicates
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle("Responder Location");
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_responder_marker));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Anchor like Google Maps pin
        mapView.getOverlays().add(marker);
    }

    // Recenter map on user's location
    private void recenterMap() {
        if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
            GeoPoint currentLocation = locationOverlay.getMyLocation();
            mapView.getController().animateTo(currentLocation); // Smooth animation
            locationOverlay.enableFollowLocation();
            Toast.makeText(this, "Recentered on your location", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.responder_menu, menu);
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            Intent profileIntent = new Intent(this, ResponderProfileActivity.class);
            startActivity(profileIntent);
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logoutProgressDialog.show();
                    new Handler().postDelayed(() -> {
                        auth.signOut();
                        Toast.makeText(responderActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                        logoutProgressDialog.dismiss();
                        redirectToLogin();
                    }, 500);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void checkIDProof() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userDoc = firestore.collection("users").document(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String idProofUrl = documentSnapshot.getString("idProofUrl");
                if (idProofUrl == null || idProofUrl.isEmpty()) {
                    new Handler().postDelayed(() -> showIDProofDialog(), 5000);
                }
            }
        });
    }

    private void showIDProofDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upload ID Proof")
                .setMessage("Please upload your ID proof to complete your profile.")
                .setCancelable(false)
                .setPositiveButton("Edit Profile", (dialog, which) -> {
                    Intent intent = new Intent(responderActivity.this, ResponderProfileEditActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(responderActivity.this, "Please provide your id proof in the profile", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
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
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            mapView.getOverlays().remove(locationOverlay);
        }
    }
}