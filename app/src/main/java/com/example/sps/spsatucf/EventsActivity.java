package com.example.sps.spsatucf;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventsActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference dref;
    private RecyclerView view;
    private String events = "events";
    private ArrayList<Entry> entries = new ArrayList<>();
    private CustomAdapter adapter;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // don't know with this does, it was populated with the file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        SetupDrawerMenu();

        String title = "Upcoming Events";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorGold)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);
        SetupDrawerMenu();


        database = FirebaseDatabase.getInstance();
        dref = database.getReference().child(events);
        adapter = new CustomAdapter(entries);

        // **********************
        view = findViewById(R.id.eventsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        view.setLayoutManager(layoutManager);
        view.setAdapter(adapter);
        // ***********************

        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entries.clear();
                for(DataSnapshot event : dataSnapshot.getChildren()){
                    Entry entry = new Entry();
                    for(DataSnapshot param : event.getChildren()){
                        switch(param.getKey()) {
                            case "title": entry.title = param.getValue().toString();
                                break;
                            case "date": entry.date = param.getValue().toString();
                                break;
                            case "loc": entry.loc = param.getValue().toString();
                                break;
                            case "description": entry.description = param.getValue().toString();
                                break;
                            case "imageloc": entry.imageloc = param.getValue().toString();
                                break;
                        }
                    }
                    entries.add(entry);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                return;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;                // consumed
        return super.onOptionsItemSelected(item);
    }
    protected void SetupDrawerMenu() {
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

                Log.d("NAV MENU", "Selection " + menuItem.getItemId());
                switch (menuItem.getItemId()) {
                    case R.id.nav_news: {
                        finish();
                        startActivity(new Intent(EventsActivity.this, NewsActivity.class));
                        break;
                    }

                    case R.id.nav_events: {
                        finish();
                        startActivity(new Intent(EventsActivity.this, EventsActivity.class));
                        break;
                    }

                    case R.id.nav_polls: {
                        finish();
                        startActivity(new Intent(EventsActivity.this, PollingActivity.class));
                        break;
                    }

                    case R.id.nav_quiz: {
                        finish();
                        startActivity(new Intent(EventsActivity.this, QuizActivity.class));
                        break;
                    }

                    case R.id.nav_review: {
                        finish();
                        startActivity(new Intent(EventsActivity.this, ReviewActivity.class));
                        break;
                    }

                    case R.id.nav_points: {
                        finish();
                        startActivity(new Intent(EventsActivity.this, PointsActivity.class));
                        break;
                    }

                    case R.id.nav_settings: {
                        finish();
                        startActivity(new Intent(EventsActivity.this, SettingsActivity.class));
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

class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<Entry> entries;

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView title, timeloc, description;
        public ImageView image;

        MyViewHolder(View view){
            super(view);
            title = (TextView)view.findViewById(R.id.title);
            timeloc = (TextView)view.findViewById(R.id.timeloc);
            description = (TextView)view.findViewById(R.id.description);
            image = (ImageView)view.findViewById(R.id.entryImage);
        }
    }

    public CustomAdapter(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(
                LayoutInflater.from(
                        viewGroup.getContext()
                ).inflate(R.layout.event_entry, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Entry entry = entries.get(i);
        myViewHolder.title.setText(entry.title);
        myViewHolder.timeloc.setText(entry.date + " @ " + entry.loc);
        myViewHolder.description.setText(entry.description);
        myViewHolder.image.setImageResource(R.drawable.sps_logo);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}

class Entry
{
    public String title;
    public String date;
    public String loc;
    public String description;
    public String imageloc;
}