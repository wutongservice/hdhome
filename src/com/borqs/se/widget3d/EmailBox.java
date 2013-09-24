package com.borqs.se.widget3d;

import com.borqs.se.event.XMLAnimation;
import com.borqs.se.object3d.SECamera.CameraChangedListener;
import com.borqs.se.scene.SE3DScene;

public class EmailBox extends NormalObject implements CameraChangedListener {
    private XMLAnimation mOpenEmailBox;

    public EmailBox(SE3DScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void initStatus(SE3DScene scene) {
        super.initStatus(scene);
        mOpenEmailBox = new XMLAnimation(getScene(), "assets/base/email/animation.xml", mIndex);
        getCamera().addCameraChangedListener(this);
        setHasInit(true);
    }

    public void onCameraChanged() {
        if (getCamera().isGroundSight()) {
          if (mOpenEmailBox != null) {
              mOpenEmailBox.setIsReversed(false);
              mOpenEmailBox.execute();
          }
        } else if (getCamera().isDefaultSight()) {
            if (mOpenEmailBox != null) {
                mOpenEmailBox.setIsReversed(true);
                mOpenEmailBox.execute();
            }
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        mOpenEmailBox.pause();
        mOpenEmailBox = null;
        getCamera().removeCameraChangedListener(this);
    }

}
