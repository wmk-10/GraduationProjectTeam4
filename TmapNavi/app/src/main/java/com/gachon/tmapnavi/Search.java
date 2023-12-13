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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.skt.Tmap.TMapGpsManager;

public class Search extends AppCompatActivity {
    TextView textView;

    public TextToSpeech tts;
    private TMapView tMapView;
    private TMapData tMapData;
    private String apiKey = "19WcpRBG1E1ttKsnYGMW92XiXrKkD8e6jx0otXhe";
    private OkHttpClient httpClient;
    private Handler handler = new Handler();
    private Runnable locationUpdateRunnable;
    TMapPoint endPoint_g;
//    TMapPoint endPoint;

    // 갱신 주기 (밀리초 단위, 예: 5000 = 5초)
    private static final int UPDATE_INTERVAL = 10000;
//    private TMapGpsManager tMapGPSManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        // Initialize OkHttpClient
        httpClient = new OkHttpClient();

//        textView = findViewById(R.id.search_id);
        initializeTMapView(); // Initialize TMapView
//        initializeLocationUpdateRunnable();
//        initializeTMapGPSManager();
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

        //TTS 사용
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.KOREAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(Search.this, "지원하지 않는 언어입니다", Toast.LENGTH_SHORT).show();
                        // Runnable을 초기화하고 시작
//        initializeLocationUpdateRunnable();
//        handler.postDelayed(locationUpdateRunnable, UPDATE_INTERVAL);

                    }
                }
            }
        });
    }

    interface GeocodeCallback {
        void onGeocodeSuccess(TMapPoint point);
        void onGeocodeFailure(Exception e);
    }

    private void geocodeAddress(String address, GeocodeCallback callback) {
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
                callback.onGeocodeFailure(e);
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
                        jsonResponse = jsonResponse.getJSONObject("coordinateInfo");
                        JSONArray jsonArray = jsonResponse.getJSONArray("coordinate");
                        jsonResponse = jsonArray.getJSONObject(0);
//                        System.out.println("jsonArray = " + jsonArray.getJSONObject(0));
                        double endlat = jsonResponse.getDouble("lat");
                        double endlon = jsonResponse.getDouble("lon");
//                        System.out.println("jsonResponse = " + jsonResponse);
                        TMapPoint endPoint = new TMapPoint(endlat, endlon);
                        System.out.println("TMapPoint = " + endPoint);
                        callback.onGeocodeSuccess(endPoint);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onGeocodeFailure(e);
                    }
                }
            }
        });
    }

    // Runnable을 초기화하는 메서드
//    private void initializeLocationUpdateRunnable() {
//        locationUpdateRunnable = new Runnable() {
//            @Override
//            public void run() {
//                getCurrentLocation(); // 현재 위치 업데이트
//                handler.postDelayed(this, UPDATE_INTERVAL); // 다음 업데이트 예약
//            }
//        };
//        handler.post(locationUpdateRunnable); // Runnable 시작
//    }

    protected void onDestroy() {
        super.onDestroy();
        // Handler에서 Runnable을 제거하여 메모리 누수 방지
        handler.removeCallbacks(locationUpdateRunnable);
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
//                        System.out.println("Received path data: " + jsonPathData);

                        JSONArray jsonArray = jsonPathData.getJSONArray("features");
                        System.out.println("jsonArray = " + jsonArray);
//                        JSONArray jsonArray = jsonPathData.getJSONArray("description");
                        jsonPathData = jsonArray.getJSONObject(0);
                        jsonPathData = jsonPathData.getJSONObject("properties");
//                        System.out.println("jsonPathData = " + jsonPathData);
                        String naviSpeech = jsonPathData.getString("description");
                        System.out.println("naviSpeech = " + naviSpeech);

                        // 숫자 문자열 추출을 위한 새 코드
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(naviSpeech);
                        StringBuilder numericStrings = new StringBuilder();
                        if (matcher.find()) {
                            // Extract the number and convert it to an integer
                            int number = Integer.parseInt(matcher.group());

                            // Perform the calculation
                            int calculatedNumber = (int) Math.round(number * 100.0 / 74.0);

                            // Replace the original number in the sentence with the calculated number
                            String newSentence = naviSpeech.replaceAll("\\d+", String.valueOf(calculatedNumber));

                            // Adjust the unit from 'meters' to 'steps'
                            newSentence = newSentence.replace("m", " 걸음");
                            tts.speak(newSentence, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        System.out.println("숫자 문자열: " + numericStrings.toString().trim());

//                        tts.speak(newSentence.toString(), TextToSpeech.QUEUE_FLUSH, null);

//                        tts.speak(naviSpeech, TextToSpeech.QUEUE_FLUSH, null);
                        // 파일에 JSON 데이터를 저장
//                        saveJsonToFile(jsonPathData);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
//        initializeLocationUpdateRunnable(); // 위치 업데이트 Runnable 초기화 및 시작
    }


    private void initializeTMapView() {
//        public void initializeTMap (MainActivity mainActivity){
        // TmapView 생성 및 초기화
        tMapView = new TMapView(this); // Use 'this' instead of 'mainActivity'
        tMapView.setSKTMapApiKey(apiKey);
        tMapData = new TMapData();
        Log.i("TMap", "초기화 성공");
        //현재위치 (파란점)
//        tMapView.setTrackingMode(true);
//        tMapView.setIconVisibility(true);
//        tMapView.setSightVisible(true);
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

    private void updateLocationData(double latitude, double longitude) {
        String address = "가천대"; // 이 주소는 필요에 따라 변경 가능
        geocodeAddress(address, new GeocodeCallback() {
            @Override
            public void onGeocodeSuccess(TMapPoint endPoint) {
                endPoint_g = new TMapPoint(endPoint.getLatitude(), endPoint.getLongitude());
                fetchPathData(latitude, longitude, endPoint.getLatitude(), endPoint.getLongitude());
                findPath(new TMapPoint(latitude, longitude), endPoint);
            }

            @Override
            public void onGeocodeFailure(Exception e) {
                Log.e("Geocode", "Geocoding failed: " + e.getMessage());
            }
        });
    }


    private void getCurrentLocation() {
        System.out.println("Search.getCurrentLocation");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("Search.onLocationChanged");
                if (location != null) {
                    double currentLatitude = location.getLatitude();
                    double currentLongitude = location.getLongitude();
                    System.out.println("currentLongitude = " + currentLongitude);
                    System.out.println("currentLatitude = " + currentLatitude);
                    System.out.println("Search.onLocationChanged");
                    updateLocationData(currentLatitude, currentLongitude);
                    System.out.println("Search.onLocationChanged finished");
//                    String address = "가천대";
//                    geocodeAddress(address, new GeocodeCallback() {
//                        @Override
//                        public void onGeocodeSuccess(TMapPoint endPoint) {
//                            endPoint_g = new TMapPoint(endPoint.getLatitude(), endPoint.getLongitude());
//                            fetchPathData(currentLatitude, currentLongitude, endPoint.getLatitude(), endPoint.getLongitude());
//                            findPath(new TMapPoint(currentLatitude, currentLongitude), endPoint);
//                        }
//
//                        @Override
//                        public void onGeocodeFailure(Exception e) {
//                            System.out.println("Fail GEOCoding");
//                        }
//                    });
                }
                else {
                    System.out.println("LOCATION FAILED!!");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
        } else {
            Log.e("Location", "Location permission not granted");
        }
    }

    private void findPath(TMapPoint startPoint, TMapPoint endPoint) {
        tMapData.findPathDataWithType(
                TMapData.TMapPathType.PEDESTRIAN_PATH,
                startPoint,
                endPoint,
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
//                                Log.d("TMap", "path = " + path);
                    }

                    public void onFindPathDataFailed(int errorType, String errorMessage) {
                        // 경로 검색 실패 시 호출되는 콜백
                        Log.e("TMap", "Error: " + errorMessage);
                    }
                }
        );
    }


}
