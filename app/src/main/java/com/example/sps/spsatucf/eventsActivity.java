package com.example.sps.spsatucf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class eventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        db = FirebaseFirestore.getInstance();
    }
}
