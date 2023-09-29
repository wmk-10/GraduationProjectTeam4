package com.gachon.tmapnavi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Search extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        textView = findViewById(R.id.search_id);

        /*
        Intent intent = getIntent();
        String place = intent.getStringExtra("search_place");

        textView.setText(place);
        System.out.println("넘어온 값: " + place );
        */

        Intent intent = getIntent();
        String[] received_places = intent.getStringArrayExtra("search_places");

        if (received_places != null) {
            //문자열 클래스
            StringBuilder placesText = new StringBuilder();

            // received_places 배열의 각 요소를 하나씩 가져와서 place 변수에 할당하는 반복문
            // place에는 배열에 있는 각 장소 정보가 순서대로 저장됨
            for (String place : received_places) {
                placesText.append(place).append(" ");
            }

            // placeText라는 StringBuilder 객체의 내용을 문자열로 반환 필요
            textView.setText(placesText.toString());
            //System.out.println("넘어온 값: " + placesText.toString());

        } else {
            textView.setText("전달받은 장소 정보가 없습니다.");
        }
    }
}
