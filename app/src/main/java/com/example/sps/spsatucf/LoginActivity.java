package com.example.sps.spsatucf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    final String sharedPrefFile = "com.example.android.spsatucf";
    SharedPreferences sharedPreferences;

    FirebaseAuth firebaseAuth;
    String email;
    String password;
    boolean rememberme = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String title = "Login";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorGold)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);


        firebaseAuth = FirebaseAuth.getInstance();

        EditText edtEmail = findViewById(R.id.edtUsername);
        EditText edtPassword = findViewById(R.id.edtPassword);

        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        rememberme = sharedPreferences.getBoolean("rememberme", true);

        if (rememberme) {
            email = sharedPreferences.getString("email", null);
            password = sharedPreferences.getString("password", null);

            if (email != null && password != null) {
                edtEmail.setText(email);
                edtPassword.setText(password);

                // attempt auto login
                AttemptSignIn();
            }
        }
    }

    public void onClicked_btnLogin(View v)
    {

        EditText edtEmail = findViewById(R.id.edtUsername);
        EditText edtPassword = findViewById(R.id.edtPassword);

        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();

        if (email.length() == 0 || password.length() == 0)
        {
            Toast.makeText(LoginActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        AttemptSignIn();
    }
    public void onClicked_btnSignup(View v)
    {
        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
    }

    protected void AttemptSignIn()
    {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // successful signin
                            // hazardous security
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            if (rememberme) {
                                editor.putString("email", email);
                                editor.putString("password", password);

                                editor.apply();
                            }

                            finish();
                            startActivity(new Intent(LoginActivity.this, NewsActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
