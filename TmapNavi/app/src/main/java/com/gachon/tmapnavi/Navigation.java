package com.gachon.tmapnavi;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Navigation extends AppCompatActivity {
    Intent intent;
    SpeechRecognizer mRecognizer;
    Button sttBtn;
    TextView textView;

    public TextToSpeech tts;
    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        if ( Build.VERSION.SDK_INT >= 23 ){
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        textView = findViewById(R.id.sttResult);
        sttBtn = findViewById(R.id.sttStart);

        //STT를 위한 음성인식 - Recognizer Intent 객체 생성
        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        sttBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecognizer=SpeechRecognizer.createSpeechRecognizer(Navigation.this);
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(intent);
            }
        });

        //TTS 사용
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.KOREAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(Navigation.this, "지원하지 않는 언어입니다", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //음성인식 콜백 메세지 정리
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            //음성 인식이 매치된 글자 arrayList에 string형태로 저장
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            //list의 글자 textView로 출력
            for(int i = 0; i < matches.size() ; i++){
                textView.setText(matches.get(i));
            }
            //arrayList를 string으로 변경
            String stc = matches.toString();

            Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
            komoran.setUserDic("userdic.txt");

            //분석기 사용할 string 입력
            KomoranResult analyzeResultList = komoran.analyze(stc);

            //분석 결과에서 형태소 정보 추출
            List<Token> tokenList = analyzeResultList.getTokenList();
            // tokenList 배열의 각 요소들을 token에 순서대로 할당 (Run, logcat에서 확인 가능)
            for (Token token : tokenList) {
                //명사 정보만 추출 후 string 변환
                //String destination = analyzeResultList.getNouns().toString();
                //System.out.println(destination);

                // 대명사 + 숫자 인식 가능 (getMorphesByTags: 원하는 형태소만 뽑기)
                System.out.println(analyzeResultList.getMorphesByTags("NNG","NNP","NNB","NP","SN"));
            }

            List<String> places = analyzeResultList.getMorphesByTags("NNG","NNP","NNB","NP","SN");
//            places = Collections.singletonList("가천대");
            if(!places.isEmpty()) {
                String[] search_places = places.toArray(new String[0]);

                System.out.println("검색할 장소: " + places);

                //길 안내 전 확인 멘트
                tts.speak(places + "으로 안내를 시작할까요?", TextToSpeech.QUEUE_FLUSH, null);

                //사용자 답변 음성인식
                mRecognizer.startListening(intent);

                //음성인식 결과 string으로 변환 후 저장
                ArrayList<String> answer =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                //list의 글자 textView로 출력
//                for(int i = 0; i < matches.size() ; i++){
//                    textView.setText(matches.get(i));
//                }
//                String anw = answer.toString();

                tts.speak("안내를 시작합니다", TextToSpeech.QUEUE_FLUSH, null);

                // 결과 설정 및 Activity 종료
                Intent new_intent = new Intent(Navigation.this, Search.class);
                new_intent.putExtra("search_places", search_places);
                startActivity(new_intent);
                //setResult(RESULT_OK, new_intent);
                finish();

//                //사용자 답변 확인
//                if (anw.contains("예") || anw.contains("네") || anw.contains("응") || anw.contains("그래") == true) {
//
//                    tts.speak("안내를 시작합니다", TextToSpeech.QUEUE_FLUSH, null);
//
//                    // 결과 설정 및 Activity 종료
//                    Intent new_intent = new Intent(Navigation.this, MainActivity.class);
//                    new_intent.putExtra("search_places", search_places);
//                    setResult(RESULT_OK, new_intent);
//                    finish();
//                } else {
//                    tts.speak("안내 시작을 종료합니다", TextToSpeech.QUEUE_FLUSH, null);
//                }

            }
            else {
                System.out.println("검색할 장소가 없습니다.");
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

}