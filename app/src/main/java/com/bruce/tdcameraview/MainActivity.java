package com.bruce.tdcameraview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bruce.lib.base.AspectRatio;
import com.bruce.lib.utils.L;
import com.bruce.lib.widget.CameraView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraView mCameraView;
    private FloatingActionButton mTakePicture;
    private FloatingActionButton mCameraSwitch;
    private FloatingActionButton mCameraFlash;

    private RadioGroup mRatioGroup;

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };
    private RxPermissions mRxPermissions;
    private int mCurrentFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.camera_view);
        mTakePicture = findViewById(R.id.fab_take_picture);
        mCameraSwitch = findViewById(R.id.fab_camera_switch);
        mCameraFlash = findViewById(R.id.fab_camera_flash);
        mRatioGroup = findViewById(R.id.rg_ratio_radio_group);

        mCameraSwitch.setOnClickListener(this);
        mCameraFlash.setOnClickListener(this);
        mTakePicture.setOnClickListener(this);

        mCameraView.addCallback(mCallback);

        mRatioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            mCameraView.setAspectRatio(mRatioList.get(checkedId));
        });

        mRxPermissions = new RxPermissions(this);

        mCurrentFlash = 0;
        setFlash(mCurrentFlash);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onResume() {
        super.onResume();
        mRxPermissions.request(Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        mCameraView.start();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_take_picture:
                if (mCameraView != null) {
                    mCameraView.takePicture();
                }
                break;

            case R.id.fab_camera_switch:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ? CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                break;

            case R.id.fab_camera_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    setFlash(mCurrentFlash);
                }
                break;
        }
    }

    private void setFlash(int flashIndex) {
        mCameraFlash.setImageResource(FLASH_ICONS[flashIndex]);
        mCameraView.setFlash(FLASH_OPTIONS[flashIndex]);
    }

    private void createAndAdd(int id, String text, boolean isChecked) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setId(id);
        radioButton.setChecked(isChecked);
        radioButton.setText(text);
        Bitmap bitmap = null;
        radioButton.setTextColor(Color.WHITE);
        radioButton.setButtonDrawable(new BitmapDrawable(bitmap));
        radioButton.setBackgroundResource(R.drawable.ratio_selector);
        radioButton.setGravity(Gravity.CENTER);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(16);
        params.setMarginStart(16);
        params.gravity = Gravity.CENTER;
        mRatioGroup.addView(radioButton, params);
    }

    private List<AspectRatio> mRatioList;
    private CameraView.Callback mCallback = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            if (mRatioGroup == null) {
                return;
            }
            mRatioGroup.removeAllViews();
            mRatioList = transform(cameraView.getSupportedAspectRations());
            AspectRatio aspectRatio = cameraView.getAspectRatio();
            int index = 0;
            for (AspectRatio ratio : mRatioList) {
                createAndAdd(index++, ratio.getX() + ":" + ratio.getY(), aspectRatio.equals(ratio));
            }
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {

        }

        @Override
        public void onPictureTaken(CameraView cameraView, byte[] date) {

        }
    };

    private List<AspectRatio> transform(Set<AspectRatio> set) {
        List<AspectRatio> list = new ArrayList<>();
        list.clear();
        list.addAll(set);
        return list;
    }

}
