package com.bruce.lib.api14;

import android.view.Surface;
import android.view.View;

import com.bruce.lib.base.AbsPreview;

public class TextureViewPreview extends AbsPreview {

    @Override
    public Surface getSurface() {
        return null;
    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public Class getOutputClass() {
        return null;
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {

    }

    @Override
    public boolean isReady() {
        return false;
    }
}
