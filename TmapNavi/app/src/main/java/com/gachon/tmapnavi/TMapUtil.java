package com.gachon.tmapnavi;

import static com.gachon.tmapnavi.MainActivity.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TMapUtil extends Activity {
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

    private TMapView tMapView;
    private TMapData tMapData;
    private String apiKey;
    private OkHttpClient httpClient;

    public TMapUtil(String apiKey) {
        this.apiKey = apiKey;
    }

    public void initializeTMap(MainActivity mainActivity) {
        // TmapView 생성 및 초기화
        tMapView = new TMapView(mainActivity);
        tMapView.setSKTMapApiKey(apiKey);
        tMapData = new TMapData();
        Log.i("TMap", "초기화 성공");

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
    }

    public TMapView getTMapView() {
        return tMapView;
    }

    private void searchPath() {
        // 위치 권한 요청 처리 필요
        System.out.println("Call permission");
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
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
    }
}
