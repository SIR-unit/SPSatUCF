package com.example.sps.spsatucf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.HashMap;

public class PointsActivity extends AppCompatActivity {
    final String sharedPrefFile = "com.example.android.spsatucf";

    final String PointsProfileTable = "points-profiles";
    final String PointsQuestionTable = "points-question";
    final String PointsField = "points";
    final String ImageField = "image";
    final String IdField = "id";
    final String CorrectAnswerField = "correctAnswer";
    final String LastIdField = "lastID";

    boolean profileFlag = false;
    boolean questionFlag = false;


    PointsProfile profile = new PointsProfile();
    PointsQuestion question = new PointsQuestion();             // only one here


    FirebaseDatabase db;
    DatabaseReference db_ref;
    FirebaseAuth firebaseAuth;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        SetupDrawerMenu();

/*        SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Toast.makeText(PointsActivity.this, "Login Required", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(PointsActivity.this, LoginActivity.class));
            return;                     // we cannot continue
        }*/

        // authenticate
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(PointsActivity.this, "Login Required", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(PointsActivity.this, LoginActivity.class));
            return;                     // we cannot continue
        }
        profile.user = firebaseAuth.getCurrentUser();

        Log.d("USER:", profile.user.getEmail());
        // read in user profile
        ReadUserProfile();
        // read in list of questions
        ReadQuestion();
/*        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            profile.user = firebaseAuth.getCurrentUser();

                            // Log.d("USER:", profile.user.getEmail());
                            // read in user profile
                            ReadUserProfile();
                            // read in list of questions
                            ReadQuestion();
                        } else {
                            Log.d("ERROR:", "Failed to authenticate");
                        }
                    }
                }
        );
        */
    }

    void ReadUserProfile() {
        db = FirebaseDatabase.getInstance();
        db_ref = db.getReference();

        db_ref.child(PointsProfileTable).child(profile.user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer points = dataSnapshot.child(PointsField).getValue(Integer.class);
                Integer lastID = dataSnapshot.child(LastIdField).getValue(Integer.class);

                points = (points == null) ? new Integer(0) : points;           // if null set to 0
                lastID = (lastID == null) ? new Integer(-1) : lastID;          // if null set to -1

                profile.lastID = lastID;
                profile.points = points;

                profileFlag = true;

                if (profileFlag && questionFlag) {
                    DisplayQuestion();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", "Error trying to get user profile" + databaseError);
            }
        });

    }


    void ReadQuestion() {
        db = FirebaseDatabase.getInstance();
        db_ref = db.getReference();

        db_ref.child(PointsQuestionTable).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer id = dataSnapshot.child(IdField).getValue(Integer.class);
                String correctAnswer = dataSnapshot.child(CorrectAnswerField).getValue(String.class);
                String imageLink = dataSnapshot.child(ImageField).getValue(String.class);
                Integer points = dataSnapshot.child(PointsField).getValue(Integer.class);

                if (id == null || correctAnswer == null || imageLink == null || points == null) {
                    Toast.makeText(PointsActivity.this, "Please reload the page.", Toast.LENGTH_SHORT);
                    return;
                }

                question.correctAnswer = correctAnswer;
                question.id = id;
                question.points = points;
                question.imageLink = imageLink;

                questionFlag = true;

                // check resources
                if (profileFlag && questionFlag) {
                    DisplayQuestion();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", "Error trying to get user profile" + databaseError);
            }
        });

    }

    void DisplayQuestion()
    {
        TextView txtPoints = findViewById(R.id.txtPoints);
        txtPoints.setText(profile.points.toString());

        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button)findViewById(R.id.btnA));
        buttons.add((Button)findViewById(R.id.btnB));
        buttons.add((Button)findViewById(R.id.btnC));
        buttons.add((Button)findViewById(R.id.btnD));
        buttons.add((Button)findViewById(R.id.btnE));

        for (Button btn : buttons)
        {
            btn.setVisibility(View.VISIBLE);
            if (profile.lastID == question.id)
                btn.setEnabled(false);
            else
                btn.setEnabled(true);

            btn.setBackgroundColor(getResources().getColor(R.color.colorGold));
        }

        TextView txtCorrect = findViewById(R.id.txtCorrect);
        txtCorrect.setVisibility(View.INVISIBLE);


        String sUrl = question.imageLink;

        // show The Image in a ImageView
        Log.e("LOADING","IMAGE: " + sUrl);
        new DownloadImageTask((ImageView) findViewById(R.id.imgQuestion))
                .execute(sUrl);

    }




    protected void AnswerChosen(char answer)
    {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button)findViewById(R.id.btnA));
        buttons.add((Button)findViewById(R.id.btnB));
        buttons.add((Button)findViewById(R.id.btnC));
        buttons.add((Button)findViewById(R.id.btnD));
        buttons.add((Button)findViewById(R.id.btnE));

        TextView txtCorrect = findViewById(R.id.txtCorrect);
        TextView txtPoints = findViewById(R.id.txtPoints);

        for (Button btn : buttons) {
            btn.setEnabled(false);
        }

        // display correct
        String correctAns = question.correctAnswer;
        int corIndex = correctAns.charAt(0) - 'A';
        int selIndex = answer - 'A';

        buttons.get(selIndex).setBackgroundColor(Color.parseColor("#555555"));
        buttons.get(corIndex).setBackgroundColor(Color.parseColor("#009900"));

        profile.lastID = question.id;
        if (answer == correctAns.charAt(0)) {
            txtCorrect.setText("CORRECT!");
            txtCorrect.setTextColor(getResources().getColor(R.color.colorCorrect));

            profile.points += question.points;

            txtPoints.setText(profile.points.toString());
        } else {
            txtCorrect.setText("INCORRECT!");
            txtCorrect.setTextColor(getResources().getColor(R.color.colorIncorrect));
        }
        txtCorrect.setVisibility(View.VISIBLE);

        // Committing to database
        CommitProfile();
    }

    protected void CommitProfile()
    {
        db_ref.child(PointsProfileTable).child(profile.user.getUid()).child(LastIdField).setValue(profile.lastID);
        db_ref.child(PointsProfileTable).child(profile.user.getUid()).child(PointsField).setValue(profile.points);
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
                        startActivity(new Intent(PointsActivity.this, NewsActivity.class));
                        break;
                    }

                    case R.id.nav_events: {
                        finish();
                        startActivity(new Intent(PointsActivity.this, EventsActivity.class));
                        break;
                    }

                    case R.id.nav_polls: {
                        finish();
                        startActivity(new Intent(PointsActivity.this, PollingActivity.class));
                        break;
                    }

                    case R.id.nav_quiz: {
                        finish();
                        startActivity(new Intent(PointsActivity.this, QuizActivity.class));
                        break;
                    }

                    case R.id.nav_review: {
                        finish();
                        startActivity(new Intent(PointsActivity.this, ReviewActivity.class));
                        break;
                    }

                    case R.id.nav_points: {
                        finish();
                        startActivity(new Intent(PointsActivity.this, PointsActivity.class));
                        break;
                    }

                    case R.id.nav_settings: {
                        finish();
                        startActivity(new Intent(PointsActivity.this, SettingsActivity.class));
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


class PointsProfile
{
    public FirebaseUser user;
    public Integer points = new Integer(0);
    public Integer lastID = new Integer(-1);
}
class PointsQuestion
{
    public Integer id = new Integer(0);
    public String correctAnswer = new String();
    public String imageLink = new String();
    public Integer points = new Integer(0);
}

