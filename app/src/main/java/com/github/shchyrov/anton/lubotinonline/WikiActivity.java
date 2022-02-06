package com.github.shchyrov.anton.lubotinonline;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Objects;

public class WikiActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "URL";

    private void updateTitle(String title) {
        setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        String url = intent.getStringExtra(EXTRA_URL);
        WebView view = findViewById(R.id.wiki_view);
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String str = view.getTitle();
                updateTitle(str);
            }
        });
        view.loadUrl(url);
    }
}