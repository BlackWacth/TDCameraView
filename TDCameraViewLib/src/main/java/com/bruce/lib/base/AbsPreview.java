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

    public abstract Surface getSurface();

    public abstract View getView();

    public abstract Class getOutputClass();

    public abstract void setDisplayOrientation(int displayOrientation);

    public abstract boolean isReady();

    public void dispatchSurfaceChanged() {
        mCallback.onSurfaceChanged();
    }

    public SurfaceHolder getSurfaceHolder() {
        return null;
    }

    public Object getSurfaceTexture() {
        return null;
    }

    public void setBufferSize(int width, int height) {

    }

    public Callback getCallback() {
        return mCallback;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public interface Callback{
        void onSurfaceChanged();
    }
}
