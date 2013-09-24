package com.borqs.se.widget3d;

import com.borqs.framework3d.home3d.HouseObject;
import com.borqs.framework3d.home3d.HouseObject.WallRadiusChangedListener;
import com.borqs.se.home3d.ModelInfo;
import com.borqs.se.scene.SE3DScene;

abstract public class WallNormalObject extends NormalObject implements WallRadiusChangedListener {
    public WallNormalObject(SE3DScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void initStatus(SE3DScene scene) {
        super.initStatus(scene);

        HouseObject wall = getWallNode();
        if (wall != null) {
            wall.addWallRadiusChangedListener(this);
        }
        setHasInit(true);
    }

    @Override
    public void onRelease() {
        super.onRelease();
        HouseObject wall = getWallNode();
        if (wall != null) {
            wall.removeWallRadiusChangedListener(this);
        }
    }

    private HouseObject getWallNode() {
        return ModelInfo.getHouseObject(getScene());
    }
}
