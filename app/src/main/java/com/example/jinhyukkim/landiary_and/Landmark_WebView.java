package com.example.jinhyukkim.landiary_and;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class Landmark_WebView extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark__web_view);

        webView = (WebView)findViewById(R.id.webView);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        webView.loadUrl(url);
        finish();
    }
}
