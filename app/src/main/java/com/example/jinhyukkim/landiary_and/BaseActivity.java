package com.example.jinhyukkim.landiary_and;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

import static android.media.CamcorderProfile.get;

/**
 * Created by ICDSP on 2018-01-14.
 */

public class BaseActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private RelativeLayout contentView = null;
    public static Context mCtx = null;

    public Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Button Btn_Pathview;
    Button Capture_btn;
    ListView listView;
    boolean list_status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.base_activity);
        mCtx = this;
        contentView = (RelativeLayout) findViewById(R.id.contentView);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView) findViewById(R.id.sV);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camera = Camera.open();
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);

                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                int degrees = 0;
                switch (rotation) {
                    case Surface.ROTATION_0: degrees = 0; break;
                    case Surface.ROTATION_90: degrees = 90; break;
                    case Surface.ROTATION_180: degrees = 180; break;
                    case Surface.ROTATION_270: degrees = 270; break;
                }
                int result  = (90 - degrees + 360) % 360;
                camera.setDisplayOrientation(result);

                camera.startPreview();
            } catch (IOException e) {
                // TODO Auto-generated catch block e.printStackTrace();
            }
        }

        listView = findViewById(R.id.itemlistview);
        listView.setVisibility(View.GONE);

        Btn_Pathview = findViewById(R.id.Btn_Pathview);
        Btn_Pathview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_status = !list_status;

                if(list_status) {
                    listView.setVisibility(View.VISIBLE);
                }   else    {
                    listView.setVisibility(View.GONE);
                }

            }
        });

        Capture_btn = (Button)findViewById(R.id.capture_BTN);
        Capture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.stopPreview();
                camera.release();
                camera = null;
                Intent landmark_class = new Intent(getApplicationContext(), landMark.class);
                startActivity(landmark_class);
            }
        });

        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext().this)
            }
        });
        */
    }



    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void setContentView(int res) {

        contentView.removeAllViews();

        LayoutInflater inflater;
        inflater = LayoutInflater.from(this);

        View item = inflater.inflate(res, null);
        contentView.addView(item, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

    }


    @Override
    public void setContentView(View view) {

        contentView.removeAllViews();

        contentView.addView(view, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

    }


    public void addView(View v) {
        contentView.removeAllViews();
        contentView.addView(v, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                int degrees = 0;
                switch (rotation) {
                    case Surface.ROTATION_0: degrees = 0; break;
                    case Surface.ROTATION_90: degrees = 90; break;
                    case Surface.ROTATION_180: degrees = 180; break;
                    case Surface.ROTATION_270: degrees = 270; break;
                }
                int result  = (90 - degrees + 360) % 360;
                camera.setDisplayOrientation(result);

                camera.startPreview();
            } catch (IOException e) { // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { // TODO Auto-generated method stub
        camera = Camera.open();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { // TODO Auto-generated method stub

    }
}