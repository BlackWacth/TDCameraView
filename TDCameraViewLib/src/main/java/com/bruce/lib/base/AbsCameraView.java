package com.bruce.lib.base;

import android.view.View;

import java.util.Set;

public abstract class AbsCameraView {

    public final Callback mCallback;

    public final AbsPreview mPreview;

    public AbsCameraView(Callback callback, AbsPreview AbsPreview) {
        mCallback = callback;
        mPreview = AbsPreview;
    }

    public View getView() {
        return mPreview.getView();
    }

    /**
     * @return 能正常打开Camera返回true
     */
    public abstract boolean start();

    public abstract boolean stop();

    public abstract boolean isCameraOpened();

    public abstract void setFacing(int facing);

    public abstract int getFacing();

    public abstract Set<AspectRatio> getSupportedAspectRatio();

    /**
     * 设置纵横比
     *
     * @param ratio 纵横比
     * @return 纵横比改变了为true
     */
    public abstract boolean setAspectRatio(AspectRatio ratio);

    public abstract boolean getAutoFocus();

    public abstract void setAutoFocus(boolean autoFocus);

    public abstract void setFlash(int flash);

    public abstract int getFlash();

    public abstract void takePicture();

    public abstract void stDisplayOrientation(int displayOrientation);

    public interface Callback {
        void onCameraOpened();

        void onCameraClosed();

        void onPictureTaken(byte[] data);
    }

}
