package com.example.bookstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    EditText email, password;
    Locale myLocale;
    String currentLang;
    MyTTS tts;
    Button sign_in;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Cart");
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        tts = new MyTTS(this);
        sign_in = findViewById(R.id.signin_button);
    }

    // the two following methods are used for voice commands purposes
    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
            if (matches.contains("login") || matches.contains("sign in")) {
                // do something if the user says login
                sign_in.performClick();
            }
        }
    }

    // helper function for the language change
    public void setLocale(String localeName) {
        myLocale = new Locale(localeName);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, MainActivity.class);
        refresh.putExtra(currentLang, localeName);
        startActivity(refresh);
    }

    // function for user's login
    public void signIn(View view){
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "LOGIN SUCCESSFUL", Toast.LENGTH_LONG).show();

                            // voice message to inform the user that he has successfully signed in
                            tts.speak("You have successfully signed in!");

                            // when the user logs in delete the books saved in the cart previously
                            reference.removeValue();

                            // redirect user to the main activity of the app, which is the one containing the available online books
                            Intent intent = new Intent(MainActivity.this, Books.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "LOGIN FAILED\n" + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            // voice message to inform the user that his login failed
                            tts.speak(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    // function for user's registration
    public void signUp(View view)
    {
        // if the user presses the button redirect him to the registration page
        startActivity(new Intent(MainActivity.this, SignUp.class));
    }

    // the following code is used for the menu bar of the application
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.greek) {setLocale("el");}
        if (id == R.id.english) {setLocale("en");}
        if (id == R.id.speak) {startVoiceRecognitionActivity();}

        return super.onOptionsItemSelected(item);
    }
}
