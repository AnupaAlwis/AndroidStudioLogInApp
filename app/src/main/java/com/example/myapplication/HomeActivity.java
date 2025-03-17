package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    TextView textView_name, textView_email, textView_randomWord, textView_rhymeWord;
    Button button_logout, button_getWord, button_rhymeWord;

    SharedPreferences sharedPreferences;

    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String API_URL = "https://random-word-api.herokuapp.com/word";

    private static String randomWor;
    private String rhymeWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        textView_email = findViewById(R.id.text_email);
        textView_name = findViewById(R.id.text_fullName);
        textView_randomWord = findViewById(R.id.text_randomWord);
        textView_rhymeWord = findViewById(R.id.text_rhymeWord);
        button_logout = findViewById(R.id.button_logout);
        button_getWord = findViewById(R.id.button_getWord);
        button_rhymeWord = findViewById(R.id.button_rhymeWord);


        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        String name = sharedPreferences.getString(KEY_NAME, null);
        String email = sharedPreferences.getString(KEY_EMAIL, null);

        if (name != null || email != null) {
            textView_name.setText("Full Name : " + name);
            textView_email.setText("Email : " + email);
        }

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
                fetchRhymeWord();
            }
        });
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
                            textView_randomWord.setText("Random Word: " + randomWor);
                            textView_rhymeWord.setText("Rhymes: " + rhymeWords);
                            Toast.makeText(HomeActivity.this, "Guess the word!", Toast.LENGTH_SHORT).show();
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


}