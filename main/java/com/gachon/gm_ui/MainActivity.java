package com.gachon.gm_ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText currentLocation;
    EditText destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLocation = findViewById(R.id.currentLocation);
        destination = findViewById(R.id.destination);

        currentLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                voiceRec();
            }
        });

        destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceRec2();
            }
        });
    }

    public void voiceRec(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        startActivityForResult(intent, 101);
    }

    public void voiceRec2(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && resultCode == RESULT_OK){
            ArrayList<String> clResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            currentLocation.setText(clResult.get(0));
        }
        else if(requestCode == 102 && resultCode == RESULT_OK){
            ArrayList<String> dResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            destination.setText(dResult.get(0));
        }
    }
}