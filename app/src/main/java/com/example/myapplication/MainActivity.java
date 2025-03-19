package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editTextName, editEmailName;
    Button button_save;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editEmailName = findViewById(R.id.editEmailName);
        button_save = findViewById(R.id.button_save);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE);

        //When open activity check whether there are sahred preference data
        String name = sharedPreferences.getString(KEY_NAME,null);

        if (name != null){
            //If data is availbale call homeactivity directly
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmailName.getText().toString();
                String name = editTextName.getText().toString();

                if(name.isEmpty() || email.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter the Details Correctly!", Toast.LENGTH_SHORT).show();
                }else{
                    //When click a button put data on shared preference
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_NAME,editTextName.getText().toString());
                    editor.putString(KEY_EMAIL,editEmailName.getText().toString());
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                    startActivity(intent);

                    Toast.makeText(MainActivity.this,"Login Success", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}