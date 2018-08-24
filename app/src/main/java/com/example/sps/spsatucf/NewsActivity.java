package com.example.sps.spsatucf;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsActivity extends AppCompatActivity {


    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        SetupDrawerMenu();

        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/news.html");
    }
/*
    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.webView);
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    */








    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;                // consumed
        return super.onOptionsItemSelected(item);
    }
    protected void SetupDrawerMenu()
    {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.closed);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Set item as selected to show that this is what we're on?

                Log.d("NAV MENU","Selection " + menuItem.getItemId());
                switch (menuItem.getItemId()) {
                    case R.id.nav_news: {
                        finish();
                        startActivity(new Intent(NewsActivity.this, NewsActivity.class));
                        break;
                    }

                    case R.id.nav_events: {
                        finish();
                        startActivity(new Intent(NewsActivity.this, EventsActivity.class));
                        break;
                    }

                    case R.id.nav_polls: {
                        finish();
                        startActivity(new Intent(NewsActivity.this, PollingActivity.class));
                        break;
                    }

                    case R.id.nav_quiz: {
                        finish();
                        startActivity(new Intent(NewsActivity.this, QuizActivity.class));
                        break;
                    }

                    case R.id.nav_review: {
                        finish();
                        startActivity(new Intent(NewsActivity.this, ReviewActivity.class));
                        break;
                    }

                    case R.id.nav_points: {
                        finish();
                        startActivity(new Intent(NewsActivity.this, PointsActivity.class));
                        break;
                    }

                    case R.id.nav_settings: {
                        finish();
                        startActivity(new Intent(NewsActivity.this, SettingsActivity.class));
                        break;
                    }
                }
                // Close drawers
                mDrawerLayout.closeDrawers();

                return true;
            }
        });
    }
}
