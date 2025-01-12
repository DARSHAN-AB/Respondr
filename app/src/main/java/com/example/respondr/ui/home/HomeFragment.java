package com.example.respondr.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.respondr.R;
import com.example.respondr.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView welcomeTextViewH,currentlocation,sosButton;
    private ImageView profileImageView, locationProfileL;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize TextView
        welcomeTextViewH = root.findViewById(R.id.welcomeTextVeiw);
        profileImageView = root.findViewById(R.id.userProfile);
        locationProfileL = root.findViewById(R.id.locationProfile);
        currentlocation = root.findViewById(R.id.current_location);
        sosButton = root.findViewById(R.id.sos_button);

        firestore = FirebaseFirestore.getInstance();

        // Get current user from FirebaseAuth
        FirebaseUser  currentUser  = FirebaseAuth.getInstance().getCurrentUser ();
        if (currentUser  != null) {
            // Retrieve the display name and set it in the TextView
            String displayName = currentUser .getDisplayName();
            welcomeTextViewH.setText("Welcome back,\n" + displayName);
            String photoUrl = currentUser .getPhotoUrl() != null ? currentUser .getPhotoUrl().toString() : null;

            // Load profile photo from Firestore
            String userId = currentUser .getUid();
            firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firestorePhotoUrl = documentSnapshot.getString("photoUrl");
                    String email = documentSnapshot.getString("email");
                    String mobile = documentSnapshot.getString("mobile");

                    // Use Firestore photo URL if it exists, otherwise use Google photo
                    if (firestorePhotoUrl != null) {
                        new LoadProfileImageTask(profileImageView).execute(firestorePhotoUrl);
                        new LoadProfileImageTask(locationProfileL).execute(firestorePhotoUrl);
                    } else {
                        new LoadProfileImageTask(profileImageView).execute(photoUrl);
                        new LoadProfileImageTask(locationProfileL).execute(photoUrl);
                    }
                } else {
                    // If no document exists, use Google photo
                    new LoadProfileImageTask(profileImageView).execute(photoUrl);
                    new LoadProfileImageTask(locationProfileL).execute(photoUrl);
                }
            });
        } else {
            // Default text if user is not signed in
            welcomeTextViewH.setText("Welcome back,\nGuest");
            profileImageView.setImageResource(R.drawable.baseline_person_24);
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Request location permissions or fetch location
        requestLocationPermissionIfNeeded();

        // Set onClickListener for SOS TextView
        sosButton.setOnClickListener(v -> openEmergencyFragment());

        return root;
    }

    private void requestLocationPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted; fetch location
            fetchLocationAndDisplay();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted; fetch location
                fetchLocationAndDisplay();
            } else {
                // Permission denied
                currentlocation.setText("Location permission denied.");
            }
        }
    }

    private void fetchLocationAndDisplay() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions not granted; do nothing
            currentlocation.setText("Permission not granted.");
            return;
        }

        // Fetch last known location
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                // Convert latitude and longitude to address
                fetchAddressFromLocation(location.getLatitude(), location.getLongitude());

                // Convert location to address
                String locationAddress = fetchAddressFromLocation(location.getLatitude(), location.getLongitude());

                // Store the location in SharedPreferences
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("current_location", locationAddress);
                editor.apply();
            } else {
                currentlocation.setText("Location unavailable. Retrying...");
                fetchUpdatedLocation(); // Trigger a new location request
            }
        }).addOnFailureListener(e -> currentlocation.setText("Failed to get location."));
    }

    private String fetchAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentlocation.setText(address.getAddressLine(0));
                return addresses.get(0).getAddressLine(0);
            } else {
                currentlocation.setText("Unable to fetch address.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            currentlocation.setText("Error fetching address.");
        }
        return "Location not available";
    }

    @SuppressLint("MissingPermission")
    private void fetchUpdatedLocation() {
        fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(1000),
                new LocationCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        fusedLocationClient.removeLocationUpdates(this); // Stop updates after receiving
                        if (!locationResult.getLocations().isEmpty()) {
                            Location location = locationResult.getLastLocation();
                            fetchAddressFromLocation(location.getLatitude(), location.getLongitude());
                        } else {
                            currentlocation.setText("Still unable to fetch location.");
                        }
                    }
                },
                Looper.getMainLooper()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Method to navigate to EmergencyFragment using NavController
    private void openEmergencyFragment() {
        // Use the NavController to navigate to the EmergencyFragment
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.emergencyFragment);
    }

    // AsyncTask to load the profile image from the URL
    private static class LoadProfileImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadProfileImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream;
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView .setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.baseline_person_24); // Default image if loading fails
            }
        }


        private Bitmap getCircularBitmap(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int size = Math.min(width, height);

            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, size, size);
            final RectF rectF = new RectF(rect);

            float radius = size / 2f;

            paint.setAntiAlias(true);
            paint.setColor(0xFFFFFFFF);

            // Draw circular bitmap
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(radius, radius, radius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }
    }
}