package com.gachon.tmapnavi;

import static com.gachon.tmapnavi.MainActivity.LOCATION_PERMISSION_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Search extends AppCompatActivity {
    TextView textView;

    public TextToSpeech tts;
    private TMapView tMapView;
    private TMapData tMapData;
    private String apiKey = "qb6k37D0lnGz1icuSeNMa0nKtOd8ztJ6uo9RNYT3";
    private OkHttpClient httpClient;
    private Handler handler = new Handler();
    private Runnable locationUpdateRunnable;

    // 갱신 주기 (밀리초 단위, 예: 5000 = 5초)
    private static final int UPDATE_INTERVAL = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        // Initialize OkHttpClient
        httpClient = new OkHttpClient();

//        textView = findViewById(R.id.search_id);
        initializeTMapView(); // Initialize TMapView

        // Add TMapView to the container
        FrameLayout mapContainer = findViewById(R.id.mapContainer);
        mapContainer.addView(tMapView);

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
//                geocodeAddress(place); // 각 주소에 대해 지오코딩 요청
            }

            String destination = placesText.toString();
            // placeText라는 StringBuilder 객체의 내용을 문자열로 반환 필요
//            textView.setText(destination);
            //System.out.println("넘어온 값: " + placesText.toString());

        } else {
            textView.setText("전달받은 장소 정보가 없습니다.");
        }

<<<<<<< Updated upstream
        //TTS 사용
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.KOREAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(Search.this, "지원하지 않는 언어입니다", Toast.LENGTH_SHORT).show();
=======
        // Runnable을 초기화하고 시작
//        initializeLocationUpdateRunnable();
//        handler.postDelayed(locationUpdateRunnable, UPDATE_INTERVAL);

    }

    private void geocodeAddress(String address) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://apis.openapi.sk.com/tmap/geo/fullAddrGeo").newBuilder();
        urlBuilder.addQueryParameter("version", "1");
        urlBuilder.addQueryParameter("format", "json");
        urlBuilder.addQueryParameter("coordType", "WGS84GEO");
        urlBuilder.addQueryParameter("fullAddr", address);

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("appKey", this.apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(myResponse);
                        // 이곳에서 JSON 데이터를 처리
                        // 예: 위도와 경도 좌표 추출
                        System.out.println("GEOCODING");
                        System.out.println("jsonResponse = " + jsonResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
>>>>>>> Stashed changes
                    }
                }
            }
        });
<<<<<<< Updated upstream
=======
    }

    // Runnable을 초기화하는 메서드
    private void initializeLocationUpdateRunnable() {
        locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                initializeTMapView(); // 현재 위치 가져오기
                handler.postDelayed(this, UPDATE_INTERVAL); // 다음 업데이트를 예약
            }
        };
    }

    protected void onDestroy() {
        super.onDestroy();
        // Handler에서 Runnable을 제거하여 메모리 누수 방지
        handler.removeCallbacks(locationUpdateRunnable);
>>>>>>> Stashed changes
    }

    private void fetchPathData(double startLat, double startLon, double endLat, double endLon) {
        RequestBody formBody = new FormBody.Builder()
                .add("startX", String.valueOf(startLon))
                .add("startY", String.valueOf(startLat))
                .add("endX", String.valueOf(endLon))
                .add("endY", String.valueOf(endLat))
                // 다른 필수 또는 선택적 매개변수들
                .add("reqCoordType", "WGS84GEO")
                .add("resCoordType", "WGS84GEO")
                .add("startName", "출발")
                .add("endName", "도착")
                .add("searchOption", "0")
                .build();

        Request request = new Request.Builder()
                .url("https://apis.openapi.sk.com/tmap/routes/pedestrian")
                .post(formBody)
                .addHeader("appKey", this.apiKey)
                .addHeader("Accept-Language", "ko")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    try {
                        JSONObject jsonPathData = new JSONObject(myResponse);
                        System.out.println("\n");
                        System.out.println("NAVIGATION");
                        // 이곳에서 JSON 데이터를 처리
                        System.out.println("Received path data: " + jsonPathData.toString());

                        // 파일에 JSON 데이터를 저장
                        saveJsonToFile(jsonPathData);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveJsonToFile(JSONObject jsonPathData) {
        String filename = "pathData.json";

        try (FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(jsonPathData.toString().getBytes());
            System.out.println("JSON data has been saved to " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeTMapView() {

//        public void initializeTMap (MainActivity mainActivity){
        // TmapView 생성 및 초기화
        tMapView = new TMapView(this); // Use 'this' instead of 'mainActivity'
        tMapView.setSKTMapApiKey(apiKey);
        tMapData = new TMapData();
        Log.i("TMap", "초기화 성공");
        //현재위치 (파란점)
        tMapView.setTrackingMode(true);
        tMapView.setIconVisibility(true);
        tMapView.setSightVisible(true);
        // API 키 인증 완료 콜백 등록
        tMapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                Log.i("TMap", "MainActivity.SKTMapApikeySucceed");
                // API 키 인증이 완료된 후에 수행할 작업
                searchPath();
            }

            @Override
            public void SKTMapApikeyFailed(String errorMsg) {
                // API 키 인증 실패 시 호출되는 콜백
                Log.e("TMap", "API Key Failed: " + errorMsg);
            }
        });

        // TmapView 추가 및 설정
        tMapView.setCenterPoint(127.1346, 37.4562);
        tMapView.setZoomLevel(15);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);

        //실시간 위치 파란점
//        tMapView.setTrackingMode(true);
//        tMapView.setIconVisibility(true);
//        tMapView.setSightVisible(true);
    }


    private void searchPath() {
        // 위치 권한 요청 처리 필요
        System.out.println("Call permission");
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용된 경우
                System.out.println("permission success");
                getCurrentLocation();
            } else {
                // 위치 권한이 거부된 경우
                Log.e("Location", "Location permission denied");
            }
        }
    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            System.out.println("lastKnownLocation = " + lastKnownLocation);

            if (lastKnownLocation != null) {
                double currentLatitude = lastKnownLocation.getLatitude();
                double currentLongitude = lastKnownLocation.getLongitude();

                String address = new String("가천대");
                geocodeAddress(address);
                // 경로 데이터 가져오기
                fetchPathData(currentLatitude, currentLongitude, 37.4562, 127.1346);
                // 경로 검색 예제
                TMapPoint startPoint = new TMapPoint(currentLatitude, currentLongitude); // 현재 위치를 출발지로 사용
                TMapPoint endPoint = new TMapPoint(37.4562, 127.1346);   // 도착지 좌표

                tMapData.findPathDataWithType(
                        TMapData.TMapPathType.PEDESTRIAN_PATH,  // 길찾기 타입 (CAR_PATH, PEDESTRIAN_PATH 등)
                        startPoint,             // 출발지 좌표
                        endPoint,               // 도착지 좌표
                        new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine path) {
                                // 경로 검색 성공 시 호출되는 콜백
                                String pathJson = String.valueOf(path.getLinePoint());
                                if (pathJson != null) {
                                    // pathJson을 사용하여 원하는 작업 수행
                                    // 예: JSON 파싱 및 정보 추출
//                                    System.out.println("pathJson = " + pathJson);
                                }
                                tMapView.addTMapPath(path);
                                Log.d("TMap", "Search Success");
                                Log.d("TMap", "path = " + path);
                            }

                            public void onFindPathDataFailed(int errorType, String errorMessage) {
                                // 경로 검색 실패 시 호출되는 콜백
                                Log.e("TMap", "Error: " + errorMessage);
                            }
                        }
                );
            } else {
                // 현재 위치를 가져오지 못한 경우 위치 업데이트를 시도
                Log.e("Location", "Failed to get current location Retry");
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // 위치 업데이트 성공 시 실행되는 부분
                        getCurrentLocation(); // 다시 현재 위치 가져오기 시도
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onProviderDisabled(String provider) {}
                }, null);
            }
        } else {
            Log.e("Location", "Location permission not granted");
        }


        //경로 받아와서 tts 출력하기 ("" 자리에 문자열 넣어주시면 됩니다!)
        tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
    }


}
