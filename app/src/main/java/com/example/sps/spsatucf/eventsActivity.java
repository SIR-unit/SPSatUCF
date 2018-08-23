package com.example.sps.spsatucf;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

public class eventsActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference dref;
    private RecyclerView view;
    private String events = "events";
    private ArrayList<Entry> entries = new ArrayList<>();
    private CustomAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // don't know with this does, it was populated with the file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        database = FirebaseDatabase.getInstance();
        dref = database.getReference(events);

        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entries.clear();
                for(DataSnapshot event : dataSnapshot.getChildren()){
                    Entry entry = new Entry();
                    for(DataSnapshot param : event.getChildren()){
                        String val = param.getKey();
                        switch(val) {
                            case "title": entry.title = val;
                                break;
                            case "date": entry.date = val;
                                break;
                            case "loc": entry.loc = val;
                                break;
                            case "description": entry.description = val;
                                break;
                            case "imageloc": entry.imageloc = val;
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