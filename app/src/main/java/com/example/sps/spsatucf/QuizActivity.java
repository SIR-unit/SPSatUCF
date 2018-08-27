package com.example.sps.spsatucf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QuizActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    final String QuizProfileTable = "quiz-profiles";
    final String QuizQuestionsTable = "quiz-questions";
    final String CurrQuestionField = "currentQuestion";
    final String PastQuestionsField = "pastQuestions";
    final String CorrectAnswerField = "correctAnswer";
    final String ImageLinkField = "image";

    final String sharedPrefFile = "com.example.android.spsatucf";
    SharedPreferences sharedPreferences;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        gestureScanner = new GestureDetector(this);


        String title = "Quiz Yourself";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorGold)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        SetupDrawerMenu();

        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        mDrawerLayout.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                //if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                //    return QuizActivity.super.onTouchEvent(event);
                return gestureScanner.onTouchEvent(event);
            }
        });


        // authenticate
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(QuizActivity.this, "Login Required", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(QuizActivity.this, LoginActivity.class));
            return;                     // we cannot continue
        }

        profile.user = firebaseAuth.getCurrentUser();

        Log.d("USER:", profile.user.getEmail());
        // read in user profile
        ReadUserProfiles();
        // read in list of questions
        ReadQuestions();
    }

    public void onClick_btnA(View v)
    {
        // A button pressed
        AnswerChosen('A');
    }
    public void onClick_btnB(View v)
    {
        // B button pressed
        AnswerChosen('B');
    }
    public void onClick_btnC(View v)
    {
        // C button pressed
        AnswerChosen('C');
    }
    public void onClick_btnD(View v)
    {
        // D button pressed
        AnswerChosen('D');
    }
    public void onClick_btnE(View v)
    {
        // E button pressed
        // Set selected button to dark grey
        AnswerChosen('E');
    }

    public void onClick_btnNext(View v)
    {
        MoveToNextQuestion();
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

    /**************************
     *
     * Utility Functions
     *
     **************************/


    protected void AnswerChosen(char answer)
    {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button)findViewById(R.id.btnA));
        buttons.add((Button)findViewById(R.id.btnB));
        buttons.add((Button)findViewById(R.id.btnC));
        buttons.add((Button)findViewById(R.id.btnD));
        buttons.add((Button)findViewById(R.id.btnE));

        TextView txtCorrect = findViewById(R.id.txtCorrect);

        for (Button btn : buttons) {
            btn.setEnabled(false);
        }

        // display correct
        String correctAns = questions.get(profile.currentQuestion).correctAnswer;
        int corIndex = correctAns.charAt(0) - 'A';
        int selIndex = answer - 'A';

        buttons.get(selIndex).setBackgroundColor(Color.parseColor("#555555"));
        buttons.get(corIndex).setBackgroundColor(Color.parseColor("#009900"));
        if (answer == correctAns.charAt(0)) {
            txtCorrect.setText("CORRECT");
            txtCorrect.setTextColor(getResources().getColor(R.color.colorCorrect));

        } else {
            txtCorrect.setText("INCORRECT");
            txtCorrect.setTextColor(getResources().getColor(R.color.colorIncorrect));
        }
        txtCorrect.setVisibility(View.VISIBLE);

        if (sharedPreferences.getBoolean("removefromquiz", true)) {
            // Committing question to database
            profile.pastQuestions.put(profile.currentQuestion, Character.toString(answer));
            CommitPastQuestions();
        }
    }
    protected void DisplayCurrentQuestion()
    {
        Log.e("SIZE","" + profile.currentQuestion.toString() + " " + Integer.toString(questions.size()));
        if (profile.currentQuestion < questions.size() && profile.currentQuestion >= 0) {
            if (profile.pastQuestions.containsKey(profile.currentQuestion))
                return;                 // do nothing here

            QuizQuestion q = questions.get(profile.currentQuestion);
            String sUrl = q.imageLink;

            // show The Image in a ImageView
            Log.e("LOADING","IMAGE: " + sUrl);
            new DownloadImageTask((ImageView) findViewById(R.id.imgQuestion))
                    .execute(sUrl);

            ResetButtons();
        } else {
            // no more questions
        }
    }
    protected void ResetButtons()
    {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button)findViewById(R.id.btnA));
        buttons.add((Button)findViewById(R.id.btnB));
        buttons.add((Button)findViewById(R.id.btnC));
        buttons.add((Button)findViewById(R.id.btnD));
        buttons.add((Button)findViewById(R.id.btnE));

        for (Button btn : buttons){
            btn.setEnabled(true);
            btn.setBackgroundColor(getResources().getColor(R.color.colorGold));
        }

        TextView txtCorrect = findViewById(R.id.txtCorrect);
        txtCorrect.setVisibility(View.INVISIBLE);
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

    protected void MoveToNextQuestion()
    {
        Log.d("PAST", "QUESTIONS: " + Integer.toString(profile.pastQuestions.size()));

        ArrayList<Integer> possibleQuestions = new ArrayList<>();
        for(Integer key : questions.keySet()) {
            if (key != profile.currentQuestion && !profile.pastQuestions.containsKey(key))                // if question exists skip
                possibleQuestions.add(key);
        }

        if (possibleQuestions.size() > 0) {       // can move forward!
            Log.d("QUESTIONS", "Possible: " + Integer.toString(possibleQuestions.size()));
            Random rand = new Random();
            Integer nextQuestion = rand.nextInt(possibleQuestions.size());  // random index into possible array

            profile.currentQuestion = possibleQuestions.get(nextQuestion);

            // Commit current question to the database
            CommitCurrentQuestion();
            DisplayCurrentQuestion();
        } else {
            Log.d("NO QUESTIONS", "NO QUESTIONS");
            Toast.makeText(this, "No more questions available for now!", Toast.LENGTH_SHORT).show();
        }
    }
    protected void MoveToPrevQuestion()
    {
        // ?? Same I think?
    }
    protected void CommitPastQuestions()
    {
        for (Integer key : profile.pastQuestions.keySet()) {
            String answers = profile.pastQuestions.get(key);
            db_ref.child(QuizProfileTable).child(profile.user.getUid()).child(PastQuestionsField).child(key.toString()).setValue(answers);
        }
    }
    protected void CommitCurrentQuestion()
    {
        db_ref.child(QuizProfileTable).child(profile.user.getUid()).child(CurrQuestionField).setValue(profile.currentQuestion);
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

                Log.d("NAV MENU","Selection " + menuItem.getItemId());
                switch (menuItem.getItemId()) {
                    case R.id.nav_news: {
                        finish();
                        startActivity(new Intent(QuizActivity.this, NewsActivity.class));
                        break;
                    }

                    case R.id.nav_events: {
                        finish();
                        startActivity(new Intent(QuizActivity.this, EventsActivity.class));
                        break;
                    }

                    case R.id.nav_polls: {
                        finish();
                        startActivity(new Intent(QuizActivity.this, PollingActivity.class));
                        break;
                    }

                    case R.id.nav_quiz: {
                        finish();
                        startActivity(new Intent(QuizActivity.this, QuizActivity.class));
                        break;
                    }

                    case R.id.nav_review: {
                        finish();
                        startActivity(new Intent(QuizActivity.this, ReviewActivity.class));
                        break;
                    }

                    case R.id.nav_points: {
                        finish();
                        startActivity(new Intent(QuizActivity.this, PointsActivity.class));
                        break;
                    }

                    case R.id.nav_settings: {
                        finish();
                        startActivity(new Intent(QuizActivity.this, SettingsActivity.class));
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

class QuizProfile
{
    public FirebaseUser user;
    public HashMap<Integer, String> pastQuestions = new HashMap<>();
    public Integer currentQuestion = new Integer(0);
}
class QuizQuestion
{
    public String correctAnswer = new String();
    public String imageLink = new String();
}

