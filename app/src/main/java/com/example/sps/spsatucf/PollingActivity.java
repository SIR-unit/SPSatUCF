package com.example.sps.spsatucf;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PollingActivity extends AppCompatActivity {

    FirebaseDatabase db;
    DatabaseReference db_ref;
    FirebaseAuth firebaseAuth;

    final String pollingTable = "poll";
    final String questionField = "question";
    final String numAnswersField = "numAnswers";
    final String answersField = "answers";
    final String resultsField = "results";

    // private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private ArrayList<ProgressBar> progressBars = new ArrayList<>();
    private ArrayList<ProgressBar> progressBars2 = new ArrayList<>();
    private ArrayList<Integer> pollingResults = new ArrayList<>();

    private int maxVote;
    private int totVotes;
    private String question;
    private Integer numAnswers;
    private ArrayList<String> answers = new ArrayList<>();

    /*
    Polling data (3 columns)
    ->Question: "How are your class?"
    ->NumAnswers: "5"
    ->Answer0:
    ->Answer1:
    ->Answer2:
    ->Answer3:
    ...
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polling);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword("bghomashi@gmail.com", "qazqaz01").addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Log.d("USER:", user.getEmail());
                        } else {
                            Log.d("ERROR:", "Failed to authenticate");
                        }
                    }
                }
        );

        SetupReference();

    }
    public void onClicked_btnBack(View v)
    {
        SetupReference();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setVisibility(View.INVISIBLE);
        btnBack.setEnabled(false);

        Button btnVote = findViewById(R.id.btnVote);
        btnVote.setVisibility(View.VISIBLE);
        btnVote.setEnabled(true);
    }
    public void onClicked_btnVote(View v)
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String resultsString = new String();


        Log.d("NUMCHECKBOXES", "BOXES: " + Integer.toString(checkBoxes.size()));
        // place answers in string
        for (int i = 0; i < checkBoxes.size(); i++)
        {
            Log.d("Checkbox" + Integer.toString(i) + " is", checkBoxes.get(i) == null ? "null" : "not null");
            CheckBox cb = checkBoxes.get(i);

            if (cb != null && cb.isChecked())
            {
                if (resultsString.length() > 0)
                    resultsString += " ";
                resultsString += Integer.toString(i);
            }
        }

        if (resultsString.length() < 0)
            return;

        Log.d("RESULT", "String-" + resultsString);

        db = FirebaseDatabase.getInstance();
        db_ref = db.getReference();                         // new reference

        db_ref.child(pollingTable).child(resultsField).child(user.getUid()).setValue(resultsString);

        db_ref.child(pollingTable).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                question = dataSnapshot.child(questionField).getValue(String.class);

                answers.clear();
                Iterable<DataSnapshot> answersSpan = dataSnapshot.child(answersField).getChildren();
                for (final DataSnapshot snapshot : answersSpan)
                    answers.add(snapshot.getValue().toString());

                // expand polling results array for all answers
                pollingResults.clear();
                for (int i = 0; i < answers.size(); i++)
                    pollingResults.add(0);

                // for each entry
                for (final DataSnapshot snapshot : dataSnapshot.child(resultsField).getChildren()) {
                    String[] votes = snapshot.getValue().toString().split(" ");     // grab values
                    // for each vote
                    for (String v : votes) {
                        int i = Integer.parseInt(v);    // vote as integer

                        if (i >= pollingResults.size())
                            continue;                   // protect against erroneous votes

                        pollingResults.set(i, pollingResults.get(i) + 1);
                    }
                }

                maxVote = pollingResults.get(0);
                totVotes = 0;
                for (int i = 1; i < pollingResults.size(); i++)
                    if (pollingResults.get(i) > maxVote)
                        maxVote = pollingResults.get(i);

                for (int i = 1; i < pollingResults.size(); i++)
                    totVotes += pollingResults.get(i);

                for (int i = 0; i < pollingResults.size(); i++)
                    Log.d("Progress: ",pollingResults.get(i).toString());

                PostVote();

                Log.d("SUCCESS", "FOUND RESULTS DB");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", "Error trying to get polling table" + databaseError);
            }
        });


    }
    protected void SetupReference()
    {
        db = FirebaseDatabase.getInstance();
        db_ref = db.getReference();

        Log.d("DATABASE", db_ref.toString());

        db_ref.child(pollingTable).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                question = dataSnapshot.child(questionField).getValue(String.class);
                numAnswers = dataSnapshot.child(numAnswersField).getValue(Integer.class);

                answers.clear();
                Iterable<DataSnapshot> answersSpan = dataSnapshot.child(answersField).getChildren();
                for (final DataSnapshot snapshot : answersSpan)
                    answers.add(snapshot.getValue().toString());

                ResetUI();

                Log.d("SUCCESS", "FOUND QUESTION DB " + question + " NUM ANSWERS " + numAnswers.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", "Error trying to get polling table" + databaseError);
            }
        });
    }
    protected void ResetUI()
    {
        TextView txtQuestion = findViewById(R.id.txtQuestion);
        txtQuestion.setText(question);

        checkBoxes.clear();
        for (int i = 0; i < answers.size(); i++)
            checkBoxes.add(null);

        RecyclerView rclView = findViewById(R.id.rclView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        rclView.setLayoutManager(layoutManager);

        RecyclerView.Adapter rclAdapter = new RecyclerAnswerAdapter(answers, numAnswers, checkBoxes);
        rclView.setAdapter(rclAdapter);


    }
    protected void PostVote()
    {
        TextView txtQuestion = findViewById(R.id.txtQuestion);
        txtQuestion.setText(question);

        RecyclerView rclView = findViewById(R.id.rclView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        rclView.setLayoutManager(layoutManager);

        RecyclerView.Adapter rclAdapter = new RecyclerProgressAdapter(answers, pollingResults, progressBars2, maxVote, totVotes);
        rclView.setAdapter(rclAdapter);

        Button btnVote = findViewById(R.id.btnVote);
        btnVote.setEnabled(false);
        btnVote.setVisibility(View.INVISIBLE);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setEnabled(true);
        btnBack.setVisibility(View.VISIBLE);
    }
}

class RecyclerAnswerAdapter extends RecyclerView.Adapter<RecyclerAnswerAdapter.ViewHolder>
{
    private Integer numAnswers = 1;
    private List<String> pollAnswers;
    private Integer numChecked = 0;
    private ArrayList<CheckBox> checkBoxes;


    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CheckBox checkBox;
        public TextView txtAnswer;
        public View layout;

        public ViewHolder(View v) {
            super(v);

            layout = v;
            checkBox = null;
            txtAnswer = null;
        }
    }

    RecyclerAnswerAdapter(List<String> answers, Integer numAnswers, ArrayList<CheckBox> checkBoxes)
    {
        this.checkBoxes = checkBoxes;
        this.numAnswers = numAnswers;
        pollAnswers = answers;
    }


    @Override
    public int getItemCount() {
        return pollAnswers.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerAnswerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        Context context = parent.getContext();

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.HORIZONTAL);

        CheckBox checkBox = new CheckBox(context);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, // unchecked
                        new int[]{android.R.attr.state_checked} , // checked
                },
                new int[]{
                        context.getResources().getColor(R.color.colorGold),
                        context.getResources().getColor(R.color.colorGold),
                }
        );
        CompoundButtonCompat.setButtonTintList(checkBox, colorStateList);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CheckBox checkbox = (CheckBox) buttonView;
                if (isChecked && numChecked >= numAnswers) {
                    checkbox.setChecked(false);
                } else {
                    // the checkbox either got unchecked
                    // or there are less than 'numChecked' other checkboxes checked
                    // change your counter accordingly
                    if (isChecked) {
                        numChecked++;
                    } else {
                        numChecked--;
                    }
                }
            }
        });
        view.addView(checkBox);


        TextView txtAnswer = new TextView(context);
        txtAnswer.setTextColor(context.getResources().getColor(R.color.colorGold));
        txtAnswer.setPadding(0, 0, 10, 0);

        view.addView(txtAnswer);
        ViewHolder vh = new ViewHolder(view);
        vh.checkBox = checkBox;
        vh.txtAnswer = txtAnswer;

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.txtAnswer.setText(pollAnswers.get(position));
        checkBoxes.set(position, holder.checkBox);
    }
}

class RecyclerProgressAdapter extends RecyclerView.Adapter<RecyclerProgressAdapter.ViewHolder>
{
    private List<String> pollAnswers;
    private ArrayList<ProgressBar> progressBars;
    private ArrayList<Integer> results;
    private int maxVote;
    private int totVotes;


    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ProgressBar progressBar;
        public TextView txtAnswer;
        public TextView txtPercent;
        public View layout;

        public ViewHolder(View v) {
            super(v);

            layout = v;
            progressBar = null;
            txtAnswer = null;
        }
    }

    RecyclerProgressAdapter(List<String> answers, ArrayList<Integer> results, ArrayList<ProgressBar> progressBars, int maxVote, int totVotes)
    {
        this.results = results;
        this.maxVote = maxVote;
        this.totVotes = totVotes;
        this.progressBars = progressBars;
        pollAnswers = answers;
    }

    @Override
    public int getItemCount() {
        return pollAnswers.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerProgressAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView txtAnswer = new TextView(context);
        txtAnswer.setTextColor(context.getResources().getColor(R.color.colorGold));
        txtAnswer.setPadding(0, 0, 10, 0);
        view.addView(txtAnswer);



        LinearLayout view2 = new LinearLayout(context);
        view2.setOrientation(LinearLayout.HORIZONTAL);
        view2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgress(0);

        Drawable drawable = progressBar.getProgressDrawable();
        drawable.setColorFilter(new LightingColorFilter(
                context.getResources().getColor(R.color.colorBlack),
                context.getResources().getColor(R.color.colorGold)));

        progressBar.setLayoutParams(new ViewGroup.LayoutParams(
                600,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        view2.addView(progressBar);

        TextView txtPercent = new TextView(context);
        txtPercent.setLayoutParams(new ViewGroup.LayoutParams(
                200,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        txtPercent.setPadding(10, 0, 10, 0);
        view2.addView(txtPercent);

        view.addView(view2);

        ViewHolder vh = new ViewHolder(view);
        vh.progressBar = progressBar;
        vh.txtAnswer = txtAnswer;
        vh.txtPercent = txtPercent;

        Log.d("LISTVIEW", "ADDING Progressbars");

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.txtAnswer.setText(pollAnswers.get(position));

        holder.txtPercent.setText(Integer.toString((int)Math.round(results.get(position) * 100.0 / totVotes)) + "%");
        holder.progressBar.setProgress((int)Math.round(results.get(position) * 100.0 / totVotes));
        // progressBars.set(position, holder.progressBar);
    }
}
