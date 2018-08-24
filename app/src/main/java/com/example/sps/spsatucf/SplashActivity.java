package com.example.sps.spsatucf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.MonthDisplayHelper;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {

    final int sleepTimer = 2;           // seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        LogoLauncher logoLauncher = new LogoLauncher();
        logoLauncher.start();
    }

    /*****************************
     * class LogoLauncher - runs timer on second thread
     * written by Noah Sapp and Brandon Belna
     *
     *****************************/

    private class LogoLauncher extends Thread {
        public void run() {
            try {
                sleep(1000 * sleepTimer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SplashActivity.this.finish();
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
    }
}
