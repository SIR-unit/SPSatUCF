package edu.ucf.sps.spsatucf;

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

import edu.ucf.sps.spsatucf.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;

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

        // creates dropdown menu and title bar.
        SetupDrawerMenu();
        String title = "Upcoming Events";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorGold)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        // get reference database, create adapter, and link adapter to the events recycler view.
        database = FirebaseDatabase.getInstance();
        dref = database.getReference().child(events);
        adapter = new CustomAdapter(entries);
        view = findViewById(R.id.eventsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        view.setLayoutManager(layoutManager);
        view.setAdapter(adapter);

        // add event listener that repopulates the entries array which is a list of entries to be
        // displayed in the events page. Notice that this listener is added to a reference to the
        // events section of the database
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entries.clear();
                GregorianCalendar now = new GregorianCalendar();

                // each event is a child of the events data snopahot
                for(DataSnapshot event : dataSnapshot.getChildren()){

                    //populate entry
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
                            case "image": entry.imageloc = param.getValue().toString();
                                break;
                        }
                    }

                    // check that time in entry is after the current time
                    String dateparts[];
                    dateparts = entry.date.split("[-/ :]");

                    int pmflag = (dateparts[4].toLowerCase().charAt(2) == 'p')? 12 : 0;

                    // hacky way to make event not disappear until an hour after an event starts.
                    // if you can think of a better way, please change.
                    if (Integer.parseInt(dateparts[3]) + pmflag + 1 < 24)
                        pmflag++;

                    // set the event time.
                    entry.time = new GregorianCalendar(Integer.parseInt(dateparts[2]),
                            Integer.parseInt(dateparts[0]),
                            Integer.parseInt(dateparts[1]) + 1,
                            Integer.parseInt(dateparts[3]) + pmflag,
                            Integer.parseInt(dateparts[4].substring(0, 2)));

                    // add this entry if it has yet to occur
                    if (now.before(entry.time))
                        entries.add(entry);
                }
                // sort entries before updating view
                Collections.sort(entries, new Comparator<Entry>() {
                    @Override
                    public int compare(Entry o1, Entry o2) {
                        return o1.time.compareTo(o2.time);
                    }
                });

                // update views by notifying the adapter.
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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

// custom adapter for the RecyclerView
class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<Entry> entries;

    // simple container class to hold views
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

    // This function reads the xml file (event_entry.xml) and turns it into a GUI element.
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(
                LayoutInflater.from(
                        viewGroup.getContext()
                ).inflate(R.layout.event_entry, viewGroup, false)
        );
    }

    // apply values to entry in eventsRecyclerView.
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Entry entry = entries.get(i);
        myViewHolder.title.setText(entry.title);
        myViewHolder.timeloc.setText(entry.date + " at " + entry.loc);
        myViewHolder.description.setText(entry.description);
        myViewHolder.image.setImageResource(R.drawable.sps_logo2);

        if (entry.imageloc.length() > 0)
            new DownloadImageTask(myViewHolder.image).execute(entry.imageloc);

    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}

// the simplest container object for each entry.
class Entry
{
    public String title;
    public String date;
    public String loc;
    public String description;
    public String imageloc;
    public GregorianCalendar time;
}
