package com.example.respondr;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class termsandconditions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_and_conditions);

        WebView webView = findViewById(R.id.webview);

        // Enable JavaScript (if needed for the HTML file)
        webView.getSettings().setJavaScriptEnabled(true);

        // Prevent opening links in the browser
        webView.setWebViewClient(new WebViewClient());

        // Load the HTML file from the assets folder
        webView.loadUrl("file:///android_asset/Terms_conditions.html");
    }
}
