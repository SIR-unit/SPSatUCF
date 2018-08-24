package com.example.sps.spsatucf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ReviewActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    final String QuizProfileTable = "quiz-profiles";
    final String QuizQuestionsTable = "quiz-questions";
    final String CurrQuestionField = "currentQuestion";
    final String PastQuestionsField = "pastQuestions";
    final String CorrectAnswerField = "correctAnswer";
    final String ImageLinkField = "image";

    final String sharedPrefFile = "com.example.android.spsatucf";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase db;
    DatabaseReference db_ref;

    GestureDetector gestureScanner;

    boolean profileLoaded = false;
    boolean questionsLoaded = false;

    QuizProfile profile = new QuizProfile();
    HashMap<Integer, QuizQuestion> questions = new HashMap<>();

    int currentQuestion = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Log.d("REVIEW", "ENTERING");

        gestureScanner = new GestureDetector(this);
        SetupDrawerMenu();

        String title = "Question Graveyard";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorGold)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        mDrawerLayout.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    return ReviewActivity.super.onTouchEvent(event);
                return gestureScanner.onTouchEvent(event);
            }
        });

/*
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Toast.makeText(ReviewActivity.this, "Login Required", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(ReviewActivity.this, LoginActivity.class));
            return;                     // we cannot continue
        }
        */
        // authenticate
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(ReviewActivity.this, "Login Required", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(ReviewActivity.this, LoginActivity.class));
            return;                     // we cannot continue
        }

        profile.user = firebaseAuth.getCurrentUser();

        Log.d("USER:", profile.user.getEmail());
        // read in user profile
        ReadUserProfiles();
        // read in list of questions
        ReadQuestions();
/*        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            profile.user = firebaseAuth.getCurrentUser();

                            Log.d("USER:", profile.user.getEmail());
                            // read in user profile
                            ReadUserProfiles();
                            // read in list of questions
                            ReadQuestions();
                        } else {
                            Log.d("ERROR:", "Failed to authenticate");
                        }
                    }
                }
        );
        */
    }


    /*******************************
     *
     * GESTURE CALLBACKS
     *
     *******************************/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureScanner.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float vX, float vY) {
        if (vX > 0) {           // Swipe right
            Log.d("GESTURE","SWIPE LEFT");
            MoveToNextQuestion();
        } else if (vX < 0) {    // swipe left
            Log.d("GESTURE","SWIPE RIGHT");
            MoveToPrevQuestion();
        } else {
            return false;
        }
        return true;
    }
    public void onClick_btnReturn(View v)
    {

        ArrayList<Integer> pQuestions = new ArrayList<>();

        // assemble list
        for (Integer k : profile.pastQuestions.keySet())
            pQuestions.add(k);

        Log.d("RETURN", "past questions before: " + Integer.toString(profile.pastQuestions.size()));

        profile.pastQuestions.remove(pQuestions.get(currentQuestion));
        Log.d("RETURN", "past questions after: " + Integer.toString(profile.pastQuestions.size()));

        db_ref.child(QuizProfileTable).
                child(profile.user.getUid()).
                child(PastQuestionsField).
                child(Integer.toString(pQuestions.get(currentQuestion))).removeValue();


        if (profile.pastQuestions.size() == 0) currentQuestion = -1;            // empty
        else if (profile.pastQuestions.size()-1 <= currentQuestion) currentQuestion = 0;  // past the end, loop back
        else if (currentQuestion > 0) currentQuestion--;                                  // find to move back

        DisplayCurrentQuestion();
    }

    /**************************
     *
     * Utility Functions
     *
     **************************/


    protected void Clear()
    {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button)findViewById(R.id.btnA));
        buttons.add((Button)findViewById(R.id.btnB));
        buttons.add((Button)findViewById(R.id.btnC));
        buttons.add((Button)findViewById(R.id.btnD));
        buttons.add((Button)findViewById(R.id.btnE));

        for (Button btn : buttons) {
            btn.setEnabled(false);
            btn.setBackgroundColor(getResources().getColor(R.color.colorGold));
        }

        TextView txtCorrect = findViewById(R.id.txtCorrect);
        txtCorrect.setVisibility(View.INVISIBLE);

        ImageView imgView = findViewById(R.id.imgQuestion);
        imgView.setVisibility(View.INVISIBLE);

        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setEnabled(false);
        btnReturn.setVisibility(View.INVISIBLE);
    }
    protected void AnswerChosen(char answer)
    {
        ArrayList<Integer> pQuestions = new ArrayList<>();

        // assemble list
        for (Integer k : profile.pastQuestions.keySet())
            pQuestions.add(k);

        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button)findViewById(R.id.btnA));
        buttons.add((Button)findViewById(R.id.btnB));
        buttons.add((Button)findViewById(R.id.btnC));
        buttons.add((Button)findViewById(R.id.btnD));
        buttons.add((Button)findViewById(R.id.btnE));

        TextView txtCorrect = findViewById(R.id.txtCorrect);

        for (Button btn : buttons) {
            btn.setEnabled(false);
            btn.setBackgroundColor(getResources().getColor(R.color.colorGold));
        }

        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setEnabled(true);
        btnReturn.setVisibility(View.VISIBLE);

        // display correct
        String correctAns = questions.get(pQuestions.get(currentQuestion)).correctAnswer;
        int corIndex = correctAns.charAt(0) - 'A';
        int selIndex = answer - 'A';

        buttons.get(selIndex).setBackgroundColor(Color.parseColor("#555555"));
        buttons.get(corIndex).setBackgroundColor(Color.parseColor("#009900"));

        if (answer == correctAns.charAt(0)) {
            txtCorrect.setText("CORRECT!");
            txtCorrect.setTextColor(getResources().getColor(R.color.colorCorrect));
        } else {
            txtCorrect.setText("INCORRECT!");
            txtCorrect.setTextColor(getResources().getColor(R.color.colorIncorrect));
        }

        txtCorrect.setVisibility(View.VISIBLE);
    }
    protected void DisplayCurrentQuestion()
    {
        ArrayList<Integer> pQuestions = new ArrayList<>();

        // assemble list
        for (Integer k : profile.pastQuestions.keySet())
            pQuestions.add(k);

        if (profile.pastQuestions.size() <= 0) {
            currentQuestion = -1;
            Clear();
        } else if (currentQuestion < pQuestions.size()) {                       // is there even a question
            QuizQuestion q = questions.get(pQuestions.get(currentQuestion));
            String sUrl = q.imageLink;

            Log.e("SIZE","" + Integer.toString(currentQuestion) + " " + Integer.toString(profile.pastQuestions.size()));

            // show The Image in a ImageView
            new DownloadImageTask((ImageView) findViewById(R.id.imgQuestion))
                    .execute(sUrl);


            AnswerChosen(profile.pastQuestions.get(pQuestions.get(currentQuestion)).charAt(0));
        } else {
            Clear();
        }
    }
    protected void ReadUserProfiles()
    {
        db = FirebaseDatabase.getInstance();
        db_ref = db.getReference();

        Log.d("DATABASE", db_ref.toString());

        // look in
        //      quiz-profile ->
        //          UUID ->
        db_ref.child(QuizProfileTable).child(profile.user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer temp = dataSnapshot.child(CurrQuestionField).getValue(Integer.class);
                profile.currentQuestion = temp == null ? 0 : temp;

                profile.pastQuestions.clear();
                Iterable<DataSnapshot> answersSpan = dataSnapshot.child(PastQuestionsField).getChildren();
                // question #, pastAnswer
                for (final DataSnapshot snapshot : answersSpan)
                    profile.pastQuestions.put(Integer.parseInt(snapshot.getKey()), snapshot.getValue().toString());

                profileLoaded = true;

                if (profile.pastQuestions.size() <= 0)
                    currentQuestion = -1;

                // check resources
                if (profileLoaded && questionsLoaded)
                    DisplayCurrentQuestion();


                Log.d("SUCCESS", "Read User Profile");
                Log.e("SUCCESS", "" + profile.currentQuestion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", "Error trying to get user profile" + databaseError);
            }
        });
    }
    protected void ReadQuestions()
    {
        db = FirebaseDatabase.getInstance();
        db_ref = db.getReference();

        Log.d("DATABASE", db_ref.toString());

        // look in
        //      quiz-questions ->
        //          question # ->
        //              correctAnswer : answer
        //              image : link
        db_ref.child(QuizQuestionsTable).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // question number
                Iterable<DataSnapshot> questionsSnap = dataSnapshot.getChildren();
                questions.clear();
                for (final DataSnapshot snapshot : questionsSnap) {
                    QuizQuestion q = new QuizQuestion();
                    q.imageLink = snapshot.child(ImageLinkField).getValue().toString();
                    q.correctAnswer = snapshot.child(CorrectAnswerField).getValue().toString();
                    questions.put(Integer.parseInt(snapshot.getKey()), q);
                }

                questionsLoaded = true;

                // check resources
                if (profileLoaded && questionsLoaded)
                    DisplayCurrentQuestion();


                Log.d("SUCCESS", "Read questions");
                Log.e("SUCCESS", "" + questions.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", "Error trying to questions" + databaseError);
            }
        });
    }

    protected void MoveToNextQuestion() {
        Log.d("PAST", "QUESTIONS: " + Integer.toString(profile.pastQuestions.size()));

        if (profile.pastQuestions.size() <= 0) {
            currentQuestion = -1;
            Toast.makeText(this, "No more questions available for now!", Toast.LENGTH_SHORT).show();
            Clear();
            Log.d("NO QUESTIONS", "NO QUESTIONS");
            return;
        } else if ((currentQuestion + 1) < profile.pastQuestions.size()) {       // can move forward!
            Log.d("QUESTIONS", "Possible: " + Integer.toString(profile.pastQuestions.size()));

            currentQuestion++;
            DisplayCurrentQuestion();
        } else { // (currentQuestion + 1) >= profile.pastQuestions.size()
            Log.d("QUESTIONS", "Possible: " + Integer.toString(profile.pastQuestions.size()));

            currentQuestion = 0;            // loop back
            DisplayCurrentQuestion();
        }
    }
    protected void MoveToPrevQuestion()
    {
        Log.d("PAST", "QUESTIONS: " + Integer.toString(profile.pastQuestions.size()));

        if (profile.pastQuestions.size() <= 0) {
            currentQuestion = -1;
            Toast.makeText(this, "No more questions available for now!", Toast.LENGTH_SHORT).show();
            Clear();

            Log.d("NO QUESTIONS", "NO QUESTIONS");
            return;
        } else if (currentQuestion > 0) {       // can move backwards!
            Log.d("QUESTIONS", "Possible: " + Integer.toString(profile.pastQuestions.size()));

            currentQuestion--;
            DisplayCurrentQuestion();
        } else {
            Log.d("QUESTIONS", "Possible: " + Integer.toString(profile.pastQuestions.size()));

            currentQuestion = profile.pastQuestions.size() - 1; // loop front
            DisplayCurrentQuestion();
        }
    }



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

                switch (menuItem.getItemId()) {
                    case R.id.nav_news: {
                        finish();
                        startActivity(new Intent(ReviewActivity.this, NewsActivity.class));
                        break;
                    }

                    case R.id.nav_events: {
                        finish();
                        startActivity(new Intent(ReviewActivity.this, EventsActivity.class));
                        break;
                    }

                    case R.id.nav_polls: {
                        finish();
                        startActivity(new Intent(ReviewActivity.this, PollingActivity.class));
                        break;
                    }

                    case R.id.nav_quiz: {
                        finish();
                        startActivity(new Intent(ReviewActivity.this, QuizActivity.class));
                        break;
                    }

                    case R.id.nav_review: {
                        finish();
                        startActivity(new Intent(ReviewActivity.this, ReviewActivity.class));
                        break;
                    }

                    case R.id.nav_points: {
                        finish();
                        startActivity(new Intent(ReviewActivity.this, PointsActivity.class));
                        break;
                    }

                    case R.id.nav_settings: {
                        finish();
                        startActivity(new Intent(ReviewActivity.this, SettingsActivity.class));
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

