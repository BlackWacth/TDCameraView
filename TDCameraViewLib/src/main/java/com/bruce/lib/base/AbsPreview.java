package com.bruce.lib.base;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * 以向后兼容的方式封装与摄像机预览相关的所有操作。
 */
public abstract class AbsPreview {

    private Callback mCallback;

    private int mWidth;
    private int mHeight;

    abstract Surface getSurface();

    abstract View getView();

    abstract Class getOutputClass();

    abstract void setDisplayOrientation(int displayOrientation);

    abstract boolean isReady();

    void dispatchSurfaceChanged() {
        mCallback.onSurfaceChanged();
    }

    SurfaceHolder getSurfaceHolder() {
        return null;
    }

    Object getSurfaceTexture() {
        return null;
    }

    void setBufferSize(int width, int height) {

    }

    public Callback getCallback() {
        return mCallback;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    interface Callback{
        void onSurfaceChanged();
    }
}
