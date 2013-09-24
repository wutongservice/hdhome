package com.borqs.se.event;

import com.borqs.se.object3d.SEObject;
import com.borqs.se.scene.SE3DScene;

public class SEAlphaAnimation extends SEEmptyAnimation {
    private SEObject mObj;
    private boolean mIsBlending;

    public SEAlphaAnimation(SE3DScene scene, SEObject obj, float from, float to, int times) {
        super(scene, from, to, times);
        mObj = obj;
    }

    @Override
    public void onFinish() {
        if (!mIsBlending) {
            mObj.setBlendingable(false, true);
        }
    }

    @Override
    public void onBegin() {
        mIsBlending = mObj.isBlendingable();
        if (!mIsBlending) {
            mObj.setBlendingable(true, true);
        }
    }

    public void onAnimationRun(float value) {
        mObj.setAlpha(value, true);
    }

}
