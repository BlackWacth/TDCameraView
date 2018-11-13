package com.bruce.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.bruce.lib.R;
import com.bruce.lib.api14.Camera1;
import com.bruce.lib.api14.TextureViewPreview;
import com.bruce.lib.api21.Camera2;
import com.bruce.lib.base.AbsCameraView;
import com.bruce.lib.base.AbsPreview;
import com.bruce.lib.base.AspectRatio;
import com.bruce.lib.base.Constants;
import com.bruce.lib.utils.L;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Set;

public class CameraView extends FrameLayout {

    /**
     * 相机与屏幕相反的方向，后置
     */
    public static final int FACING_BACK = Constants.FACING_BACK;

    /**
     * 相机与屏幕相同的方向，前置
     */
    public static final int FACING_FRONT = Constants.FACING_FRONT;

    /**
     * 相机相对于屏幕的方向。
     */
    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    /**
     * 关闭闪光灯
     */
    public static final int FLASH_OFF = Constants.FLASH_OFF;

    /**
     * 拍照期间始终开启闪光灯。
     * 根据驱动程序的不同，在预览或自动对焦期间也可能会闪光。
     */
    public static final int FLASH_ON = Constants.FLASH_ON;

    /**
     * 预览，自动对焦和拍照期间持续发光.
     * 这也可以用于视频录制。
     */
    public static final int FLASH_TORCH = Constants.FLASH_TORCH;

    /**
     * 闪光灯将在需要时自动触发。
     * 闪光灯可能会在预览，自动对焦或快照期间闪光，具体取决于驱动程序。
     */
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    /**
     * 闪光灯将在防红眼模式下闪光
     */
    public static final int FLASH_RED_EYE = Constants.FLASH_RED_EYE;

    /**
     * 相机设备闪光控制的模式
     */
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    public @interface Flash {
    }

    private AbsCameraView mAbsCameraView;

    private final CallbackBridge mCallbacks;

    private boolean mAdjustViewBounds;

    private final DisplayOrientationDetector mDisplayOrientationDetector;

    public CameraView(@NonNull Context context) {
        this(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //判断是否可视化编辑
        if (isInEditMode()) {
            mCallbacks = null;
            mDisplayOrientationDetector = null;
            return;
        }

        final AbsPreview preview = createAbsPreview(context);
        mCallbacks = new CallbackBridge();
        //todo 判断SDK版本，后续做
//        mAbsCameraView = new Camera1(mCallbacks, preview);
        mAbsCameraView = new Camera2(context, mCallbacks, preview);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraView, defStyleAttr, R.style.Widget_CameraView);
        mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
        setFacing(a.getInt(R.styleable.CameraView_facing, FACING_BACK));
        String aspectRatio = a.getString(R.styleable.CameraView_aspectRatio);
        if (aspectRatio != null) {
            setAspectRatio(AspectRatio.parse(aspectRatio));
        } else {
            setAspectRatio(Constants.DEFAULT_ASPECT_RATIO);
        }
        setAutoFocus(a.getBoolean(R.styleable.CameraView_autoFocus, true));
        setFlash(a.getInt(R.styleable.CameraView_flash, Constants.FLASH_AUTO));
        a.recycle();

        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisPlayOrientationChanged(int displayOrientation) {
                L.eee("hzw", "displayOrientation = %d", displayOrientation);
                mAbsCameraView.setDisplayOrientation(displayOrientation);
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mDisplayOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInEditMode()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (mAdjustViewBounds) {
            if (!isCameraOpened()) {
                mCallbacks.reserveRequestLayoutOnOpen();
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                //todo width固定，求高，比例 width * y / x
//                int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio.toFloat());
                int height = MeasureSpec.getSize(widthMeasureSpec) * ratio.getY() / ratio.getX();
                if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio.toFloat());
                if (widthMode == MeasureSpec.AT_MOST) {
                    width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        AspectRatio ratio = getAspectRatio();
        if (mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
            ratio = ratio.inverse();
        }
        if (height < width * ratio.getY() / ratio.getX()) {
            mAbsCameraView.getView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width * ratio.getY() / ratio.getX(), MeasureSpec.EXACTLY));
        } else {
            mAbsCameraView.getView().measure(MeasureSpec.makeMeasureSpec(height * ratio.getX() / ratio.getY(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        SaveState state = new SaveState(super.onSaveInstanceState());
        state.facing = getFacing();
        state.ratio = getAspectRatio();
        state.autoFocus = getAutoFocus();
        state.flash = getFlash();
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SaveState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SaveState ss = (SaveState) state;
        setFacing(ss.facing);
        setAspectRatio(ss.ratio);
        setAutoFocus(ss.autoFocus);
        setFlash(ss.flash);
    }

    public void start() {
        if (!mAbsCameraView.start()) {
            Parcelable state = onSaveInstanceState();
            mAbsCameraView = new Camera1(mCallbacks, createAbsPreview(getContext()));
            onRestoreInstanceState(state);
            mAbsCameraView.start();
        }
    }

    public void stop() {
        mAbsCameraView.stop();
    }

    public void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public boolean isCameraOpened() {
        return mAbsCameraView.isCameraOpened();
    }

    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (mAdjustViewBounds != adjustViewBounds) {
            mAdjustViewBounds = adjustViewBounds;
            requestLayout();
        }
    }

    public boolean getAdjustViewBounds() {
        return mAdjustViewBounds;
    }

    public void setFlash(int flash) {
        mAbsCameraView.setFlash(flash);
    }

    public int getFlash() {
        return mAbsCameraView.getFlash();
    }

    public void setAutoFocus(boolean autoFocus) {
        mAbsCameraView.setAutoFocus(autoFocus);
    }

    public boolean getAutoFocus() {
        return mAbsCameraView.getAutoFocus();
    }

    public void setAspectRatio(AspectRatio ratio) {
        if (mAbsCameraView.setAspectRatio(ratio)) {
            requestLayout();
        }
    }

    public AspectRatio getAspectRatio() {
        return mAbsCameraView.getAspectRatio();
    }

    public void setFacing(@Facing int facing) {
        mAbsCameraView.setFacing(facing);
    }

    @Facing
    public int getFacing() {
        return mAbsCameraView.getFacing();
    }

    public Set<AspectRatio> getSupportedAspectRations() {
        return mAbsCameraView.getSupportedAspectRatio();
    }

    public void takePicture() {
        mAbsCameraView.takePicture();
    }

    private AbsPreview createAbsPreview(Context context) {
        //todo 判断SDK版本，后续做
        return new TextureViewPreview(context, this);
    }

    protected static class SaveState extends BaseSavedState {

        @Facing
        int facing;

        AspectRatio ratio;

        boolean autoFocus;

        @Flash
        int flash;

        @RequiresApi(api = Build.VERSION_CODES.N)
        public SaveState(Parcel source, ClassLoader loader) {
            super(source);
            facing = source.readInt();
            ratio = source.readParcelable(loader);
            autoFocus = source.readByte() != 0;
            flash = source.readInt();
        }

        public SaveState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(facing);
            out.writeParcelable(ratio, 0);
            out.writeByte((byte) (autoFocus ? 1 : 0));
            out.writeInt(flash);
        }

        public static final Parcelable.Creator<SaveState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SaveState>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public SaveState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SaveState(parcel, classLoader);
            }

            @Override
            public SaveState[] newArray(int i) {
                return new SaveState[i];
            }
        });
    }

    private class CallbackBridge implements AbsCameraView.Callback {

        private final ArrayList<Callback> mCallbacks = new ArrayList<>();

        private boolean mRequestLayoutOnOpen;

        public CallbackBridge() {
        }

        public void add(Callback callback) {
            mCallbacks.add(callback);
        }

        public void remove(Callback callback) {
            mCallbacks.remove(callback);
        }

        public void reserveRequestLayoutOnOpen() {
            mRequestLayoutOnOpen = true;
        }

        @Override
        public void onCameraOpened() {
            if (mRequestLayoutOnOpen) {
                mRequestLayoutOnOpen = false;
                requestLayout();
            }
            for (Callback callback : mCallbacks) {
                callback.onCameraOpened(CameraView.this);
            }
        }

        @Override
        public void onCameraClosed() {
            for (Callback callback : mCallbacks) {
                callback.onCameraClosed(CameraView.this);
            }
        }

        @Override
        public void onPictureTaken(byte[] data) {
            for (Callback callback : mCallbacks) {
                callback.onPictureTaken(CameraView.this, data);
            }
        }
    }


    public abstract static class Callback {

        /**
         * 打开相机时调用
         *
         * @param cameraView cameraView
         */
        public void onCameraOpened(CameraView cameraView) {

        }

        /**
         * 关闭相机时调用
         *
         * @param cameraView cameraView
         */
        public void onCameraClosed(CameraView cameraView) {

        }

        /**
         * 拍照时候调用
         *
         * @param cameraView cameraView
         * @param date       JPEG data
         */
        public void onPictureTaken(CameraView cameraView, byte[] date) {

        }
    }
}
