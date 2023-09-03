package com.gachon.tmapnavi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import androidx.annotation.NonNull;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.TMapData.FindPathDataListenerCallback;
import com.skt.Tmap.TMapData.TMapPathType;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private TMapView tMapView;
    private TMapData tMapData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TmapView 생성 및 초기화
        System.out.println("start");
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("U2MkVFE58e6FiYuqxboyA4EWM47rcWb1CsGpJro7");
        tMapData = new TMapData();
        System.out.println("initialize success");

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
        tMapView.setCenterPoint(126.9780, 37.5665);
        tMapView.setZoomLevel(15);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);

        // 액티비티 레이아웃에 TmapView 추가
        setContentView(tMapView);
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

                // 경로 검색 예제
                TMapPoint startPoint = new TMapPoint(currentLatitude, currentLongitude); // 현재 위치를 출발지로 사용
                TMapPoint endPoint = new TMapPoint(37.5759, 126.9768);   // 도착지 좌표

                tMapData.findPathDataWithType(
                        TMapPathType.PEDESTRIAN_PATH,  // 길찾기 타입 (CAR_PATH, PEDESTRIAN_PATH 등)
                        startPoint,             // 출발지 좌표
                        endPoint,               // 도착지 좌표
                        new FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine path) {
                                // 경로 검색 성공 시 호출되는 콜백
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
