package com.borqs.se.widget3d;

import com.borqs.se.object3d.SEObject;
import com.borqs.se.scene.SE3DScene;

public class Airship extends Flyer {

    public Airship(SE3DScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void initStatus(SE3DScene scene) {
        super.initStatus(scene);
        setBanner(new SEObject(scene, getObjectInfo().mModelInfo.mChildNames[0]));
        setBannerImageKey("assets/base/feiting/ggq_01.jpg");
        setHasInit(true);
    }

    @Override
    public void onAnimationRun(int count, int TotalFrames) {

    }

//    @Override
//    protected void trimAdOff() {
//
//    }

}
