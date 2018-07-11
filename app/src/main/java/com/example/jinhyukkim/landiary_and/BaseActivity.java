package com.example.jinhyukkim.landiary_and;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.IOException;

/**
 * Created by ICDSP on 2018-01-14.
 */

public class BaseActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private RelativeLayout contentView = null;
    private static Context mCtx = null;

    private Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.base_activity);
        mCtx = this;
        contentView = (RelativeLayout) findViewById(R.id.contentView);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camera = Camera.open();
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e) {
                // TODO Auto-generated catch block e.printStackTrace();
            }
        }
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
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}