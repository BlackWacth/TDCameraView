package com.bruce.lib.widget;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

import com.bruce.lib.utils.L;

public abstract class DisplayOrientationDetector {

    private final OrientationEventListener mOrientationEventListener;

    public static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private Display mDisplay;

    private int mLastKnownDisplayOrientation = 0;

    public DisplayOrientationDetector(Context context) {
        mOrientationEventListener = new OrientationEventListener(context) {

            //这是Surface.Rotation_0，_90，_180，_270或-1（无效）
            private int mLastKnownRotation = -1;

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || mDisplay == null) {
                    return;
                }
                final int rotation = mDisplay.getRotation();
                L.www("hzw", "rotation = %d, orientation = %d", rotation, orientation);
                if (mLastKnownRotation != rotation) {
                    mLastKnownRotation = rotation;
                    dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(rotation));
                }
            }
        };
    }

    public void enable(Display display) {
        mDisplay = display;
        mOrientationEventListener.enable();
        dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(display.getRotation()));
    }

    public void disable() {
        mOrientationEventListener.disable();
        mDisplay = null;
    }

    public int getLastKnownDisplayOrientation() {
        return mLastKnownDisplayOrientation;
    }

    public void dispatchOnDisplayOrientationChanged(int displayOrientation) {
        mLastKnownDisplayOrientation = displayOrientation;
        onDisPlayOrientationChanged(displayOrientation);
    }

    /**
     * 更改显示方向时调用。
     * @param displayOrientation 0, 90, 180, 270其中之一
     */
    public abstract void onDisPlayOrientationChanged(int displayOrientation);

}
