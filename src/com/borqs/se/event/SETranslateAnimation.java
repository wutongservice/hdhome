package com.borqs.se.event;
import com.borqs.se.SEVector.SEVector3f;
import com.borqs.se.object3d.SEObject;
import com.borqs.se.scene.SE3DScene;

public class SETranslateAnimation extends SEEmptyAnimation {
    private SEObject mObj;
    private SEVector3f mFrom;
    private SEVector3f mTo;
    private boolean mIsLocal;

    public SETranslateAnimation(SE3DScene scene, SEObject obj, SEVector3f from, SEVector3f to, int times) {
        super(scene, 0, 1, times);
        mObj = obj;
        mFrom = from;
        mTo = to;
        mIsLocal = false;
    }

    public void setIsLocal(boolean isLocal) {
        mIsLocal = isLocal;
    }

    @Override
    public void onAnimationRun(float value) {
        SEVector3f distance = mTo.subtract(mFrom);
        SEVector3f location = mFrom.add(distance.mul(value));
        if (mIsLocal) {
            mObj.setLocalTranslate(location);
        } else {
            mObj.setTranslate(location, true);
        }
    }

}
