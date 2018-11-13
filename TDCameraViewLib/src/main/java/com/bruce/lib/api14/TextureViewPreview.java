package com.bruce.lib.api14;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.bruce.lib.R;
import com.bruce.lib.base.AbsPreview;
import com.bruce.lib.utils.L;

import java.util.Arrays;

public class TextureViewPreview extends AbsPreview {

    private final TextureView mTextureView;
    private int mDisplayOrientation;

    public TextureViewPreview(Context context, ViewGroup parent) {
//        final View view = View.inflate(context, R.layout.texture_view, parent);
        View view = LayoutInflater.from(context).inflate(R.layout.texture_view, parent, true);
        mTextureView = view.findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                L.iii("hzw", "onSurfaceTextureAvailable => width = %d, height = %d", width, height);
                setSize(width, height);
                configureTransform();
                dispatchSurfaceChanged();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                L.iii("hzw", "onSurfaceTextureSizeChanged => width = %d, height = %d", width, height);
                setSize(width, height);
                configureTransform();
                dispatchSurfaceChanged();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                setSize(0, 0);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    public Surface getSurface() {
        return new Surface(mTextureView.getSurfaceTexture());
    }

    @Override
    public Object getSurfaceTexture() {
        return mTextureView.getSurfaceTexture();
    }

    @Override
    public View getView() {
        return mTextureView;
    }

    @Override
    public Class getOutputClass() {
        return SurfaceTexture.class;
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        configureTransform();
    }

    @Override
    public boolean isReady() {
        return mTextureView.getSurfaceTexture() != null;
    }

    /**
     * 基于mDisplayOrientation和surface大小配置TextureView的变换矩阵
     */
    private void configureTransform() {
        Matrix matrix = new Matrix();
        L.i("hzw", "configureTransform => mDisplayOrientation = " + mDisplayOrientation);
        if (mDisplayOrientation % 180 == 90) {
            int width = getWidth();
            int height = getHeight();
            float[] src = new float[]{
                    0.0f, 0.0f,      //左上
                    width, 0.0f,     //右上
                    0.0f, height,    //左下
                    width, height    //右下
            };
            float[] dst;
            if (mDisplayOrientation == 90) {
                dst = new float[]{
                        0.0f, height,  //左上
                        0.0f, 0.0f,    //右上
                        width, height, //左下
                        width, 0.0f    //右下
                };
            } else {
                dst = new float[]{
                        width, 0.0f,   //左上
                        width, height, //右上
                        0.0f, 0.0f,    //左下
                        0.0f, height   //右下
                };
            }
            L.i("hzw", "src = " + Arrays.toString(src));
            L.i("hzw", "dst = " + Arrays.toString(dst));
            //从src矩阵坐标，变换到dst矩阵坐标
            matrix.setPolyToPoly(src, 0, dst, 0, 4);
        } else if (mDisplayOrientation == 180){
            L.i("hzw", "configureTransform 旋转180");
            matrix.postRotate(180, getWidth() / 2, getHeight() / 2);
        }
        L.i("hzw", "configureTransform 不做旋转");
        mTextureView.setTransform(matrix);
    }
}
