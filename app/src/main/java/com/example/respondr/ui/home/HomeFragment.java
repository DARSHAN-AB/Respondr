package com.example.respondr.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.respondr.R;
import com.example.respondr.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView welcomeTextViewH;
    private ImageView profileImageView, locationProfileL;

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

        // Get current user from FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Retrieve the display name and set it in the TextView
            String displayName = currentUser.getDisplayName();
            welcomeTextViewH.setText("Welcome back,\n" + displayName);
            String photoUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null;

            // Load profile photo from URL
            if (photoUrl != null) {
                new LoadProfileImageTask(profileImageView).execute(photoUrl);
                new LoadProfileImageTask(locationProfileL).execute(photoUrl);
            } else {
                // Default profile image
                profileImageView.setImageResource(R.drawable.baseline_person_24);
            }
        } else {
            // Default text if user is not signed in
            welcomeTextViewH.setText("Welcome back,\nGuest");
            profileImageView.setImageResource(R.drawable.baseline_person_24);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.baseline_person_24); // Fallback image
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