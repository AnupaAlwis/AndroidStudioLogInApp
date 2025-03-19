package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {
    TextView textView_name, textView_email, textView_rhymeWord, textView_score, textView_length, timerText;
    Button button_logout, button_getWord, button_rhymeWord, button_submitGuess, button_askLength, button_submitLetter, button_leaderBoard;
    EditText guessed_word, text_submit_letter;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String API_URL = "https://random-word-api.herokuapp.com/word";
    private static String randomWor;
    private String rhymeWords;
    private String guessedWord;
    private Integer playerScore = 100;
    private Integer attempt = 0;
    private Integer helped_times = 0;
    private int seconds = 0;
    private boolean isRunning = false;
    private Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        textView_email = findViewById(R.id.text_email);
        textView_name = findViewById(R.id.text_fullName);
        textView_rhymeWord = findViewById(R.id.text_rhymeWord);
        textView_score = findViewById(R.id.text_score);
        textView_length = findViewById(R.id.text_length);
        text_submit_letter = findViewById(R.id.text_submit_letter);
        guessed_word = findViewById(R.id.guessed_word);
        timerText = findViewById(R.id.timerText);
        button_logout = findViewById(R.id.button_logout);
        button_getWord = findViewById(R.id.button_getWord);
        button_submitLetter = findViewById(R.id.button_submitLetter);
        button_askLength = findViewById(R.id.button_askLength);
        button_rhymeWord = findViewById(R.id.button_rhymeWord);
        button_leaderBoard = findViewById(R.id.button_leaderBoard);
        button_submitGuess = findViewById(R.id.button_submitGuess);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        String name = sharedPreferences.getString(KEY_NAME, null);
        String email = sharedPreferences.getString(KEY_EMAIL, null);

        if (name != null || email != null) {
            textView_name.setText(name + " !");
            textView_email.setText("Email : " + email);
            textView_score.setText("Score: " + playerScore);
        }

        button_leaderBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,LeaderBoard.class);
                startActivity(intent);
            }
        });

        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Toast.makeText(HomeActivity.this, "Log Out Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        button_getWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fetchRandomWord();

            }
        });

        button_rhymeWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(randomWor == null){
                    Toast.makeText(HomeActivity.this,"Start the Game First!",Toast.LENGTH_SHORT).show();
                }else{
                    showRhymeWords();
                }

            }
        });

        button_submitGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnswer();
            }
        });

        button_askLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askLength();
            }
        });

        button_submitLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLetter();
            }
        });
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && playerScore > 0) {
                seconds++;
                timerText.setText("Time: " + seconds + "s");
                handler.postDelayed(this, 1000);
            }
        }
    };
    private void startTimer() {
        if (!isRunning) {
            seconds = 0; // Reset timer
            isRunning = true;
            handler.post(runnable);
        }
    }
    private void submitLetter() {
        String letter = text_submit_letter.getText().toString().trim(); // Trim to remove extra spaces

        if (letter.isEmpty()) { // Check if the input is empty
            Toast.makeText(HomeActivity.this, "Please enter a letter first", Toast.LENGTH_SHORT).show();
            return; // Stop execution if the input is empty
        }

        if (letter.length() > 1) { // Ensure only a single letter is entered
            Toast.makeText(HomeActivity.this, "Enter a single letter", Toast.LENGTH_SHORT).show();
            return;
        }

        char character = letter.charAt(0);
        int count = 0;

        // Count occurrences of the letter in randomWor
        for (int i = 0; i < randomWor.length(); i++) {
            if (randomWor.charAt(i) == character) {
                count++;
            }
        }

        Toast.makeText(HomeActivity.this, "The letter occurs " + count + " times", Toast.LENGTH_SHORT).show();
        reduceScore(5);
    }
    private void askLength(){
        if(randomWor == null){
            Toast.makeText(HomeActivity.this,"Start the Game First!", Toast.LENGTH_SHORT).show();
        }else{
            if(playerScore > 0){
                reduceScore(5);
                textView_length.setText("The Length of the word is: " + randomWor.length());
            }else{
                //Toast.makeText(HomeActivity.this,"Game Over!",Toast.LENGTH_SHORT).show();
                textView_score.setText("Game Over the word is " + randomWor);
                String name = sharedPreferences.getString(KEY_NAME, null);
                sendScoreToAPI(name, playerScore, seconds);
                playerScore = 100;
                reset();
                fetchRandomWord();
            }
        }
    }
    private void submitAnswer() {
        if(!(randomWor == null || randomWor.isEmpty())){
            attempt += 1;
            guessedWord = guessed_word.getText().toString();
            if(guessedWord.equals(randomWor)){
                Toast.makeText(HomeActivity.this,"Well done!",Toast.LENGTH_SHORT).show();
                String name = sharedPreferences.getString(KEY_NAME, null);
                sendScoreToAPI(name, playerScore, seconds);
                reset();
                fetchRandomWord();
            }
            else{
                reduceScore(10);
                Toast.makeText(HomeActivity.this, "Try Again!", Toast.LENGTH_SHORT).show();
                if(playerScore <= 0){
                    isRunning = false; // Stop the timer
                    handler.removeCallbacks(runnable);
                    Toast.makeText(HomeActivity.this,"Game Over word is: " + randomWor,Toast.LENGTH_SHORT).show();
                    String name = sharedPreferences.getString(KEY_NAME, null);
                    sendScoreToAPI(name, playerScore, seconds);
                    playerScore = 100;
                    reset();
                    fetchRandomWord();
                }
            }
        }else{
            Toast.makeText(HomeActivity.this,"Please start the game first!",Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchRhymeWord() {
        if (randomWor == null || randomWor.isEmpty()) {
            fetchRandomWord();
            return;
        }
        OkHttpClient client = new OkHttpClient();

        String RHYME_URL = String.format("https://api.api-ninjas.com/v1/rhyme?word=%s", randomWor);

        Request request = new Request.Builder()
                .url(RHYME_URL)
                .addHeader("X-Api-Key", "hUN6n5P+9tG/vGR5hf4l5w==lSGgffbJGR3eZQT9")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Failed to fetch rhyme: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "Error: " + response.code(), Toast.LENGTH_LONG).show()
                    );
                    return;
                }
                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    if (jsonArray.length() > 0) {
                        // Build a string of rhyme words
                        StringBuilder rhymes = new StringBuilder();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            rhymes.append(jsonArray.getString(i)).append(", ");
                        }
                        rhymeWords = rhymes.substring(0, rhymes.length() - 2); // Remove last comma
                        runOnUiThread(() -> {

                            textView_score.setText("Score: " + playerScore);
                            Toast.makeText(HomeActivity.this, "Guess the word!", Toast.LENGTH_SHORT).show();
                            startTimer();
                        });
                    } else {
                        // No rhymes found, fetch a new word
                        runOnUiThread(() -> fetchRandomWord());
                    }
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
    private void fetchRandomWord() {
        textView_length.setText("");
//        randomWor = "apple";
//        runOnUiThread(() -> fetchRhymeWord());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://random-word-api.herokuapp.com/word")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Failed to fetch word: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "Error: " + response.code(), Toast.LENGTH_LONG).show()
                    );
                    return;
                }
                String responseData = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    randomWor = jsonArray.getString(0);
                    // Check for rhymes immediately
                    runOnUiThread(() -> fetchRhymeWord());
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
    private void showRhymeWords(){
        if((attempt>5) & (helped_times == 0)){
            String[] rhymes_array = rhymeWords.split(",");
            textView_rhymeWord.setText("The Word Rhymes with: " + rhymes_array[0]);
            helped_times += 1;
        } else if (helped_times > 0) {
            Toast.makeText(HomeActivity.this, "Sorry Already Helped", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(HomeActivity.this, "You should try atleast 5 times", Toast.LENGTH_SHORT).show();
        }
    }
    private void reduceScore(Integer point){
        playerScore -= point;
        textView_score.setText("Score: " + playerScore);
    }
    private void reset(){
        textView_rhymeWord.setText("");
        textView_score.setText("Score: " + playerScore);
        textView_length.setText("");
        guessed_word.setText("");
        text_submit_letter.setText("");
        timerText.setText("Timer: 0");
        randomWor = null;
        attempt = 0;
        helped_times = 0;
        seconds = 0;

    }

    private void sendScoreToAPI(String playerName, int playerScore, int seconds) {
        OkHttpClient client = new OkHttpClient();

        String url = String.format("http://dreamlo.com/lb/9C5QulbteEq1QG81mfCnYQh8Y5sie5T0io5A_j8WI9eA/add/%s/%d/%d",
                playerName, playerScore, seconds);

        Request request = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(null, new byte[0]))  // Empty POST body
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Failed to send score: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Score submitted successfully!", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

}