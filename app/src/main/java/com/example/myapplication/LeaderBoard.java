package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderBoard extends AppCompatActivity {
    TextView textViewLeaderboard;
    Button game_page;
    private static final String API_URL = "http://dreamlo.com/lb/9C5QulbteEq1QG81mfCnYQh8Y5sie5T0io5A_j8WI9eA/json";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);

        textViewLeaderboard = findViewById(R.id.textViewLeaderboard);
        game_page = findViewById(R.id.button_gamePage);

        fetchLeaderboard();

        game_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeaderBoard.this,HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchLeaderboard() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject leaderboard = response.getJSONObject("dreamlo").getJSONObject("leaderboard");

                            List<PlayerEntry> playerList = new ArrayList<>();

                            if (leaderboard.has("entry")) {
                                JSONArray entries;

                                if (leaderboard.get("entry") instanceof JSONObject) {
                                    entries = new JSONArray();
                                    entries.put(leaderboard.getJSONObject("entry"));
                                } else {
                                    entries = leaderboard.getJSONArray("entry");
                                }

                                for (int i = 0; i < entries.length(); i++) {
                                    JSONObject entry = entries.getJSONObject(i);
                                    String name = entry.getString("name");
                                    int score = entry.getInt("score");
                                    int time = entry.getInt("seconds");

                                    double ratio = (time > 0) ? (double) score / time : 0; // Avoid division by zero
                                    playerList.add(new PlayerEntry(name, score, time, ratio));
                                }

                                // Sort list based on score/time ratio in descending order
                                Collections.sort(playerList, new Comparator<PlayerEntry>() {
                                    @Override
                                    public int compare(PlayerEntry p1, PlayerEntry p2) {
                                        return Double.compare(p2.ratio, p1.ratio);
                                    }
                                });

                                // Build leaderboard text
                                StringBuilder leaderboardText = new StringBuilder("Leaderboard:\n\n");
                                for (int i = 0; i < playerList.size(); i++) {
                                    PlayerEntry player = playerList.get(i);
                                    leaderboardText.append(i + 1).append(". ")
                                            .append(player.name).append(" - Score: ")
                                            .append(player.score).append(", Time: ")
                                            .append(player.time).append("s, Ratio: ")
                                            .append(String.format("%.2f", player.ratio)).append("\n");
                                }

                                textViewLeaderboard.setText(leaderboardText.toString());
                            } else {
                                textViewLeaderboard.setText("No leaderboard data available.");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LeaderBoard.this, "Error parsing leaderboard", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LeaderBoard.this, "Failed to fetch leaderboard", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

    static class PlayerEntry {
        String name;
        int score;
        int time;
        double ratio;

        PlayerEntry(String name, int score, int time, double ratio) {
            this.name = name;
            this.score = score;
            this.time = time;
            this.ratio = ratio;
        }
    }
}
