package com.borqs.se.widget3d;

import com.borqs.se.event.XMLAnimation;
import com.borqs.se.scene.SE3DScene;

public class Navigation extends WallNormalObject {
    private XMLAnimation mNavigationAnim;
    private boolean mIsOpen;

    public Navigation(SE3DScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void initStatus(SE3DScene scene) {
        super.initStatus(scene);
        mNavigationAnim = new XMLAnimation(getScene(), getObjectInfo().mModelInfo.mAssetsPath + "/animation.xml",
                mIndex);
        mIsOpen = false;
    }

    public void onWallRadiusChanged(int faceIndex) {
        if (isPressed()) {
            return;
        }
        if (faceIndex == getObjectInfo().getSlotIndex()) {
            if (mNavigationAnim != null && !mIsOpen) {
                mIsOpen = true;
                mNavigationAnim.setIsReversed(false);
                mNavigationAnim.execute();
            }
        } else {
            if (mNavigationAnim != null && mIsOpen) {
                mIsOpen = false;
                mNavigationAnim.setIsReversed(true);
                mNavigationAnim.execute();
            }
        }

    }

    @Override
    public void onRelease() {
        super.onRelease();
        mNavigationAnim.pause();
        mNavigationAnim = null;
    }
}
