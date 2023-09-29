package com.gachon.tmapnavi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import androidx.annotation.NonNull;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.TMapData.FindPathDataListenerCallback;
import com.skt.Tmap.TMapData.TMapPathType;

import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TMapView tMapView;
    private TMapData tMapData;
    private Handler locationUpdateHandler;
    private Runnable locationUpdateRunnable;
    static final TMapPoint enddst = null;
    private OkHttpClient httpClient;
    private String apikey = "YOUR_API_KEY";
    private  TextView gestureText;
    private GestureDetector gDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureText = (TextView) findViewById(R.id.gestureStatusText);

        this.gDetector = new GestureDetector(this,this);
        gDetector.setOnDoubleTapListener(this);


        // TmapView 생성 및 초기화
        System.out.println("start");
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey(apikey);
        tMapData = new TMapData();
        System.out.println("initialize success");

        httpClient = new OkHttpClient();

        locationUpdateHandler = new Handler();
        locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrentLocation(); // 주기적으로 현재 위치 업데이트
                locationUpdateHandler.postDelayed(this, 3000); // 60초마다 업데이트 (원하는 주기로 수정 가능)
            }
        };

        // API 키 인증 완료 콜백 등록
        tMapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                // API 키 인증이 완료된 후에 수행할 작업
                System.out.println("MainActivity.SKTMapApikeySucceed");
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

        // 액티비티 레이아웃에 TmapView 추가
//        FrameLayout tmapContainer = findViewById(R.id.tmap_container);
//        tmapContainer.addView(tMapView);
//        tmapContainer.setVisibility(View.VISIBLE);
//        setContentView(tMapView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gDetector.onTouchEvent(event);
        // 오버라이딩한 슈퍼 클래스의 메서드를 호출한다.
        Log.i("tag","onTouchEvent");
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        //gestureText.setText("onSingleTapConfirmed");
        Log.i("tag","onSingleTapConfirmed");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        //gestureText.setText("onDoubleTap");
        Log.i("tag","onDoubleTap");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        //gestureText.setText("onDoubleTapEvent");
        Log.i("tag","onDoubleTapEvent");

        // DoubleTap하면 Navigation 화면으로 넘어감
        Intent intent = new Intent(getApplicationContext(),Navigation.class);
        startActivity(intent);

        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        //gestureText.setText("onDown");
        Log.i("tag","onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        //gestureText.setText("onShowPress");
        Log.i("tag","onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        //gestureText.setText("onSingleTapUp");
        Log.i("tag","onSingleTapUp");
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        //gestureText.setText("onScroll");
        Log.i("tag","onScroll");
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        //gestureText.setText("onLongPress");
        Log.i("tag","onLongPress");

        Intent intent = new Intent(getApplicationContext(),Navigation.class);
        startActivity(intent);
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        //gestureText.setText("onFling");
        Log.i("tag","onFling");
        return true;
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
                .addHeader("appKey", apikey)
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


    private void searchPath() {
        // 위치 권한 요청 처리 필요
        System.out.println("Call permission");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            System.out.println("lastKnownLocation = " + lastKnownLocation);

            if (lastKnownLocation != null) {
                double currentLatitude = lastKnownLocation.getLatitude();
                double currentLongitude = lastKnownLocation.getLongitude();

                // 경로 데이터 가져오기
                fetchPathData(currentLatitude, currentLongitude, 37.4562, 127.1346);

                // 경로 검색 예제
                TMapPoint startPoint = new TMapPoint(currentLatitude, currentLongitude); // 현재 위치를 출발지로 사용
                TMapPoint endPoint = new TMapPoint(37.4562, 127.1346);   // 도착지 좌표

                tMapData.findPathDataWithType(
                        TMapPathType.PEDESTRIAN_PATH,  // 길찾기 타입 (CAR_PATH, PEDESTRIAN_PATH 등)
                        startPoint,             // 출발지 좌표
                        endPoint,               // 도착지 좌표
                        new FindPathDataListenerCallback() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == -1) { // Navigation Activity를 시작할 때 사용한 요청 코드
            if (resultCode == RESULT_OK) {
                FrameLayout tmapContainer = findViewById(R.id.tmap_container);
                tmapContainer.addView(tMapView);
                tmapContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Destroy");
        // 액티비티가 종료될 때 위치 업데이트 중지
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
    }
}
