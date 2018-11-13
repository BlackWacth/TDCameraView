package com.bruce.lib.api14;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.v4.util.SparseArrayCompat;
import android.view.SurfaceHolder;

import com.bruce.lib.base.AbsCameraView;
import com.bruce.lib.base.AbsPreview;
import com.bruce.lib.base.AspectRatio;
import com.bruce.lib.base.Constants;
import com.bruce.lib.base.Size;
import com.bruce.lib.base.SizeMap;
import com.bruce.lib.utils.L;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
public class Camera1 extends AbsCameraView {

    private static final int INVALID_CAMERA_ID = -1;

    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    static {
        FLASH_MODES.put(Constants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(Constants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(Constants.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(Constants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(Constants.FLASH_RED_EYE, Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    private int mCameraId;
    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private final SizeMap mPreviewSizes = new SizeMap();
    private final SizeMap mPictureSizes = new SizeMap();
    private AspectRatio mAspectRatio;
    private boolean mShowingPreview;
    private boolean mAutoFocus;
    private int mFacing;
    private int mFlash;
    private int mDisplayOrientation;

    public Camera1(AbsCameraView.Callback callback, AbsPreview preview) {
        super(callback, preview);
        preview.setCallback(() -> {
            if (mCamera != null) {
                setupPreview();
                adjustCameraParameters();
            }
        });
    }

    private void setupPreview() {
        try {
            if (mPreview.getOutputClass() == SurfaceHolder.class) {
                L.i("hzw", "SurfaceHolder");
                final boolean needsToStopPreview = mShowingPreview;
                if (needsToStopPreview) {
                    mCamera.stopPreview();
                }
                mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                if (needsToStopPreview) {
                    mCamera.startPreview();
                }
            } else {
                L.i("hzw", "SurfaceTexture");
                mCamera.setPreviewTexture((SurfaceTexture) mPreview.getSurfaceTexture());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置camera参数
     */
    private void adjustCameraParameters() {
        L.i("hzw", "adjustCameraParameters");
        SortedSet<Size> sizes = mPreviewSizes.sizes(mAspectRatio);
        if (sizes == null) {
            mAspectRatio = chooseAspectRatio();
            sizes = mPreviewSizes.sizes(mAspectRatio);
        }
        L.d("hzw", "mAspectRatio " + mAspectRatio.toString());
        Size size = chooseOptimalSize(sizes);
        Size pictureSize = mPictureSizes.sizes(mAspectRatio).last();
        if (mShowingPreview) {
            mCamera.stopPreview();
        }
        L.iii("hzw", "PreviewSize => width = %d, height = %d", size.getWidth(), size.getHeight());
        mCameraParameters.setPreviewSize(size.getWidth(), size.getHeight());
        L.www("hzw", "PictureSize => width = %d, height = %d", size.getWidth(), size.getHeight());
        mCameraParameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        int rotation = calcCameraRotation(mDisplayOrientation);
        L.eee("hzw", "Rotation => mDisplayOrientation = %d, rotation = %d", mDisplayOrientation, rotation);
        mCameraParameters.setRotation(rotation);
        setAutoFocusInternal(mAutoFocus);
        setFlashInternal(mFlash);
        mCamera.setParameters(mCameraParameters);
        if (mShowingPreview) {
            mCamera.startPreview();
        }
    }

    /**
     * 设置闪光灯参数
     *
     * @param flash 闪光灯参数
     * @return CameraParameters被修改返回true
     */
    private boolean setFlashInternal(int flash) {
        L.iii("hzw", "flash = %d, isCameraOpened = %b", flash, isCameraOpened());
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFlashModes();
            String mode = FLASH_MODES.get(flash);
            if (modes != null && modes.contains(mode)) {
                mCameraParameters.setFlashMode(mode);
                mFlash = flash;
                return true;
            }
            String currentMode = FLASH_MODES.get(mFlash);
            if (modes == null || !modes.contains(currentMode)) {
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlash = Constants.FLASH_OFF;
                return true;
            }
            return false;
        } else {
            mFlash = flash;
            return false;
        }
    }

    /**
     * 设置聚焦类型
     *
     * @param autoFocus 是否自动聚焦
     * @return CameraParameters被修改返回true
     */
    private boolean setAutoFocusInternal(boolean autoFocus) {
        L.iii("hzw", "autoFocus = %b, isCameraOpened = %b", autoFocus, isCameraOpened());
        mAutoFocus = autoFocus;
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            } else {
                mCameraParameters.setFocusMode(modes.get(0));
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * 计算camera角度
     * 此计算通过Exif Orientation标记或实际转换bitmap应用于输出JPEG。 （由供应商的相机API决定）
     * 这与显示方向的计算方法不同
     *
     * @param screenOrientationDegrees 屏幕角度
     * @return 旋转图像以使其正确查看的度数
     */
    private int calcCameraRotation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (mCameraInfo.orientation + screenOrientationDegrees) * 360;
        } else {
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    /**
     * 计算显示方向, 用于预览
     * 这与camera角度的计算方法不同
     *
     * @param screenOrientationDegrees 屏幕角度
     * @return 旋转预览所需的度数
     */
    private int calcDisplayOrientation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
        } else {
            return (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
    }

    /**
     * 从小到大的筛选，选着与控件大小最接近的一个
     *
     * @param sizes sizes
     * @return 最接近的size
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        if (!mPreview.isReady()) {
            return sizes.first();
        }
        int desiredWidth;
        int desiredHeight;

        final int surfaceWidth = mPreview.getWidth();
        final int surfaceHeight = mPreview.getHeight();

        if (isLandscape(mDisplayOrientation)) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = null;
        for (Size size : sizes) {
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                return size;
            }
            result = size;
        }
        L.i("hzw", "result = " + result.toString());
        return result;
    }

    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == Constants.LANDSCAPE_90 ||
                orientationDegrees == Constants.LANDSCAPE_270);
    }

    /**
     * 如果有默认的宽高比，则选择默认的宽高比
     * 如果没有默认的宽高比，则选择宽高比值最大的。
     *
     * @return AspectRatio对象
     */
    private AspectRatio chooseAspectRatio() {
        AspectRatio r = null;
        for (AspectRatio ratio : mPreviewSizes.ratios()) {
            L.i("ratio = " + ratio.toString());
            r = ratio;
            if (ratio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                L.i("hzw", "AspectRatio = " + r.toString());
                return ratio;
            }
        }
        L.i("hzw", "AspectRatio = " + r.toString());
        return r;
    }

    @Override
    public boolean start() {
        chooseCamera();
        openCamera();
        if (mPreview.isReady()) {
            setupPreview();
        }
        mShowingPreview = true;
        mCamera.startPreview();
        return true;
    }

    /**
     * 打开摄像头
     */
    private void openCamera() {
        if (mCamera != null) {
            releaseCamera();
        }
        L.i("打开摄像头");
        mCamera = Camera.open(mCameraId);
        mCameraParameters = mCamera.getParameters();

        mPreviewSizes.clear();
        for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
            mPreviewSizes.add(new Size(size.width, size.height));
        }

        mPictureSizes.clear();
        for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
            mPictureSizes.add(new Size(size.width, size.height));
        }

        if (mAspectRatio == null) {
            mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
        }

        adjustCameraParameters();

        L.iii("hzw", "mDisplayOrientation = %d, display = %d", mDisplayOrientation, calcDisplayOrientation(mDisplayOrientation));
        mCamera.setDisplayOrientation(calcDisplayOrientation(mDisplayOrientation));
        mCallback.onCameraOpened();
    }



    /**
     * 释放
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCallback.onCameraClosed();
        }
    }

    /**
     * 选择相机ID
     */
    private void chooseCamera() {
        int count = Camera.getNumberOfCameras();
        for (int i = 0; i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == mFacing) {
                mCameraId = i;
                return;
            }
        }
        mCameraId = INVALID_CAMERA_ID;
        L.i("hzw", "mCameraId = " + mCameraId);
    }

    @Override
    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mShowingPreview = false;
        releaseCamera();
    }

    @Override
    public boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    public void setFacing(int facing) {
        if (mFacing == facing) {
            return;
        }
        mFacing = facing;
        if (isCameraOpened()) {
            stop();
            start();
        }
    }

    @Override
    public int getFacing() {
        return mFacing;
    }

    @Override
    public Set<AspectRatio> getSupportedAspectRatio() {
        SizeMap idealAspectRatio = mPreviewSizes;
        for (AspectRatio aspectRatio : idealAspectRatio.ratios()) {
            if (mPictureSizes.sizes(aspectRatio) == null) {
                idealAspectRatio.remove(aspectRatio);
            }
        }
        return idealAspectRatio.ratios();
    }

    @Override
    public boolean setAspectRatio(AspectRatio ratio) {
        if (mAspectRatio == null || !isCameraOpened()) {
            mAspectRatio = ratio;
            return true;
        } else if (!mAspectRatio.equals(ratio)) {
            final Set<Size> sizes = mPreviewSizes.sizes(ratio);
            if (sizes == null) {
                throw new UnsupportedOperationException(ratio + " 不支持");
            } else {
                mAspectRatio = ratio;
                adjustCameraParameters();
                return true;
            }
        }
        return false;
    }

    @Override
    public AspectRatio getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    public boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return mAutoFocus;
        }
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    @Override
    public void setAutoFocus(boolean autoFocus) {
        if (mAutoFocus == autoFocus) {
            return;
        }
        if (setAutoFocusInternal(autoFocus)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    public void setFlash(int flash) {
        if (flash == mFlash) {
            return;
        }
        if (setFlashInternal(flash)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    public int getFlash() {
        return mFlash;
    }

    @Override
    public void takePicture() {
        if (!isCameraOpened()) {
            throw new IllegalStateException("相机为启动，先启动再拍照.");
        }
        if (getAutoFocus()) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus((success, camera) -> {
                takePictureInternal();
            });
        }
    }

    private void takePictureInternal() {
        if (!isPictureCaptureInProgress.getAndSet(true)) {
            mCamera.takePicture(null, null, (data, camera) -> {
                isPictureCaptureInProgress.set(false);
                mCallback.onPictureTaken(data);
                camera.cancelAutoFocus();
                camera.startPreview();
            });
        }
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
        if (mDisplayOrientation == displayOrientation) {
            return;
        }
        mDisplayOrientation = displayOrientation;
        if (isCameraOpened()) {
            mCameraParameters.setRotation(calcCameraRotation(displayOrientation));
            mCamera.setParameters(mCameraParameters);
            final boolean needToStopPreview = mShowingPreview;
            if (needToStopPreview) {
                mCamera.stopPreview();
            }
            mCamera.setDisplayOrientation(calcDisplayOrientation(displayOrientation));
            if (needToStopPreview) {
                mCamera.startPreview();
            }
        }
    }
}
