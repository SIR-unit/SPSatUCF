package edu.ucf.sps.spsatucf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import edu.ucf.sps.spsatucf.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    final String sharedPrefFile = "edu.ucf.android.spsatucf";

    final String QuizProfileTable = "quiz-profiles";
    final String PastQuestionsField = "pastQuestions";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SetupDrawerMenu();

        String title = "Settings";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorGold)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        CheckBox chkRememberMe = findViewById(R.id.chkRememberMe);
        CheckBox chkRemoveQuiz = findViewById(R.id.chkRemoveQuiz);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean rememberme = sharedPreferences.getBoolean("rememberme", true);
        boolean removefromquiz = sharedPreferences.getBoolean("removefromquiz", true);

        chkRememberMe.setChecked(rememberme);
        chkRemoveQuiz.setChecked(removefromquiz);
    }



    public void onClicked_chkRemoveQuiz(View v) {
        CheckBox chkRememberMe = findViewById(R.id.chkRemoveQuiz);
        if (chkRememberMe.isChecked()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("removefromquiz", true);

            editor.apply();
        } else {        // unchecked
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("removefromquiz", false);

            editor.apply();
        }
    }

    public void onClicked_chkRememberMe(View v)
    {
        // preference change
        CheckBox chkRememberMe = findViewById(R.id.chkRememberMe);
        if (chkRememberMe.isChecked()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("rememberme", true);

            editor.apply();
        } else {                //uncheckedx
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("rememberme", false);
            editor.remove("email");
            editor.remove("password");

            editor.apply();
        }
    }

    public void onClicked_btnResetQuiz(View v)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference db_ref = db.getReference();



        db_ref.child(QuizProfileTable).child(user.getUid()).child(PastQuestionsField).removeValue();


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
                        startActivity(new Intent(SettingsActivity.this, NewsActivity.class));
                        break;
                    }

                    case R.id.nav_events: {
                        finish();
                        startActivity(new Intent(SettingsActivity.this, EventsActivity.class));
                        break;
                    }

                    case R.id.nav_polls: {
                        finish();
                        startActivity(new Intent(SettingsActivity.this, PollingActivity.class));
                        break;
                    }

                    case R.id.nav_quiz: {
                        finish();
                        startActivity(new Intent(SettingsActivity.this, QuizActivity.class));
                        break;
                    }

                    case R.id.nav_review: {
                        finish();
                        startActivity(new Intent(SettingsActivity.this, ReviewActivity.class));
                        break;
                    }

                    case R.id.nav_points: {
                        finish();
                        startActivity(new Intent(SettingsActivity.this, PointsActivity.class));
                        break;
                    }

                    case R.id.nav_settings: {
                        finish();
                        startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
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
