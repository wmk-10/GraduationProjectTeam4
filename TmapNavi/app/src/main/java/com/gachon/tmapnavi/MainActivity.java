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

    static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TMapView tMapView;
    private TMapData tMapData;
    private Handler locationUpdateHandler;
    private Runnable locationUpdateRunnable;
    static final TMapPoint enddst = null;
    private OkHttpClient httpClient;
    private String apikey = "IcKTqBDL9J5Gsc2VIc3Fx8gql8LFDWgi4dWC7iUi";
    private  TextView gestureText;
    private GestureDetector gDetector;
    private TMapUtil tMapUtil;
    private int requestCode;
    private int resultCode;
    @Nullable
    private Intent data;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureText = (TextView) findViewById(R.id.gestureStatusText);

        this.gDetector = new GestureDetector(this,this);
        gDetector.setOnDoubleTapListener(this);
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

}
