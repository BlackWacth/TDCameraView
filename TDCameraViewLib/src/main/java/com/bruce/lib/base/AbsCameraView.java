package com.bruce.lib.base;

import android.view.View;

import java.util.Set;

public abstract class AbsCameraView {

    final Callback mCallback;

    final AbsPreview mAbsPreview;

    public AbsCameraView(Callback callback, AbsPreview AbsPreview) {
        mCallback = callback;
        mAbsPreview = AbsPreview;
    }

    View getView() {
        return mAbsPreview.getView();
    }

    /**
     *
     * @return 能正常打开Camera返回true
     */
    abstract boolean start();

    abstract boolean stop();

    abstract boolean isCameraOpened();

    abstract void setFacing(int facing);

    abstract int getFacing();

    abstract Set<AspectRatio> getSupportedAspectRatio();

    /**
     * 设置纵横比
     * @param ratio 纵横比
     * @return 纵横比改变了为true
     */
    abstract boolean setAspectRatio(AspectRatio ratio);

    abstract boolean getAutoFocus();

    abstract void setAutoFocus(boolean autoFocus);

    abstract void setFlash(int flash);

    abstract int getFlash();

    abstract void takePicture();

    abstract void stDisplayOrientation(int displayOrientation);

    interface Callback{
        void onCameraOpened();

        void onCameraClosed();

        void onPictureTaken(byte[] data);
    }

}
