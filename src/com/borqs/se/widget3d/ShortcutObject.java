package com.borqs.se.widget3d;

import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcelable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.borqs.framework3d.home3d.HouseObject;
import com.borqs.se.LoadResThread;
import com.borqs.freehdhome.R;
import com.borqs.se.SEVector.SERect3D;
import com.borqs.se.SEVector.SEVector3f;
import com.borqs.se.ToastUtils;
import com.borqs.se.event.SECommand;
import com.borqs.se.home3d.ApplicationMenu.PreViewObject;
import com.borqs.se.home3d.ModelInfo;
import com.borqs.se.home3d.SE3DHomeScene;
import com.borqs.se.home3d.SEHomeUtils;
import com.borqs.se.object3d.SEBitmap;
import com.borqs.se.object3d.SEBitmap.Type;
import com.borqs.se.object3d.SEObject;
import com.borqs.se.object3d.SEObjectFactory;
import com.borqs.se.scene.SE3DScene;
import com.borqs.se.scene.SESceneManager;
import com.borqs.se.shortcut.AppItemInfo;
import com.borqs.se.shortcut.ItemInfo;

public class ShortcutObject extends AppObject {

    @Override
    public boolean load(final SEObject parent, final Runnable finish) {
        LoadResThread.getInstance().process(new Runnable() {
            public void run() {
                ResolveInfo resolveInfo = SEHomeUtils.findResolveInfoByComponent(getScene().getContext(),
                        getObjectInfo().mComponentName);
                HouseObject houseObject = ModelInfo.getHouseObject(getScene());
                int w = getAppIconWidth(getContext());//(int) houseObject.getWallUnitSizeX() * getObjectInfo().getSpanX();
                int h = w;//(int) houseObject.getWallUnitSizeY() * getObjectInfo().getSpanY();
                //Bitmap bitmapWithText = null;
                AppItemInfo.BitmapInfo bitmapInfo = null;
                int bitmapW = 128;
                int bitmapH = 128;
                String title = getObjectInfo().mDisplayName;
                if (getObjectInfo().mShortcutIcon == null) {
                    if (resolveInfo == null) {
                        Bitmap icon ;
                        if(SEHomeUtils.LOCKSCREEN_HOMEHD_PKG.equals(getObjectInfo().mComponentName.getPackageName())) {
                            //TODO
                            icon = ToastUtils.decodeLockScreenHdIcon(bitmapW, bitmapH, R.drawable.lockscreenhd_icon);
                        }else {
                            icon = ToastUtils.decodeDefaultApplicationIcon(bitmapW, bitmapH);
                        }
                        bitmapInfo = new AppItemInfo.BitmapInfo();
                        bitmapInfo.bitmap = icon;
                        bitmapInfo.bitmapWidth = icon.getWidth();
                        bitmapInfo.bitmapHeight = icon.getHeight();
                        bitmapInfo.bitmapContentWidth = icon.getWidth();
                        bitmapInfo.bitmapContentHeight = icon.getHeight();
                    } else {
                        //iconwithText = AppItemInfo.getBitmap(resolveInfo, w, h);
                        bitmapInfo = AppItemInfo.getBitmap(resolveInfo);
                    }
                } else {
                    /*
                    if (title != null) {
                        //For app update from old version the old shortcut,
                        //please do not use TextUtit isEmpty method to instead of it.
                        bitmapWithText = AppItemInfo.getBitmapWithText(getObjectInfo().mShortcutIcon.getBitmap(), title, w, h);
                        getObjectInfo().mShortcutIcon.recycle();
                    }
                    */
                    bitmapInfo = AppItemInfo.createBitmapInfo(getObjectInfo().mShortcutIcon.getBitmap(), bitmapW, bitmapH);
                }
                SEBitmap seBitmap;

                if (bitmapInfo == null) {
                    //For app update from old version the old shortcut,
                    seBitmap = getObjectInfo().mShortcutIcon;
                } else {
                    seBitmap = new SEBitmap(bitmapInfo.bitmap,SEBitmap.Type.normal);
                }
                final float currentBitmapWidth = bitmapInfo.bitmapWidth;
                final float currentBitmapHeight = bitmapInfo.bitmapHeight;
                final float currentBitmapContentWidth = bitmapInfo.bitmapContentWidth;
                final float currentBitmapContentHeight = bitmapInfo.bitmapContentHeight;
                String imageKey  = getObjectInfo().mName + "_imageKey";
                String imageName = getObjectInfo().mName + "_imageName";
                SERect3D rect = new SERect3D(w, h);
                mIconObject = new SEObject(getScene(), mName + "_icon");
                SEObjectFactory.createRectangle(mIconObject, rect, imageName, imageKey,seBitmap);
                float scale = 1;
                if (bitmapW > 128) {
                    scale = 128f / bitmapW;
                    bitmapH = (int) (bitmapH * scale);
                    bitmapW = 128;

                }
                mIconObject.setImageSize(bitmapW, bitmapH);
                new SECommand(getScene()) {
                    public void run() {
                        render();
                        setBitmapSize(currentBitmapWidth, currentBitmapHeight, currentBitmapContentWidth, currentBitmapContentHeight);
                        initStatus(getScene());
                        if (finish != null) {
                            finish.run();
                        }
                    }
                }.execute();
            }
        });
        return true;
    }
    @Override
    public  void onRemoveFromParent(SEObject parent) {
        Log.i("short cut", "remove");
    }
    @Override
    public void handOnClick() {
        Intent intent = getObjectInfo().getIntent();
        if (intent != null) {
            int x = getTouchX();
            int y = getTouchY();
            intent.setSourceBounds(new Rect(x, y, x, y));
            if (!SESceneManager.getInstance().startActivity(intent)) {
                if (SEHomeUtils.DEBUG)
                    Log.e(SEHomeUtils.TAG, "not found bind activity");
            }
        }
    }

    @Override
    public IconBox changeToIconBox() {
        if (getChangedToObj() == null) {
            ModelInfo modelInfo = getScene().mSceneInfo.findModelInfo("group_iconbox");
            final ObjectInfo objInfo = new ObjectInfo();
            objInfo.setModelInfo(modelInfo);
            objInfo.mIndex = (int)System.currentTimeMillis();
            objInfo.mSceneName = getScene().mSceneName;
            objInfo.mComponentName = getObjectInfo().mComponentName;
            NormalObject parent = (NormalObject) getParent();
            objInfo.mObjectSlot.set(getObjectSlot());
            objInfo.mShortcutUrl = getObjectInfo().mShortcutUrl;
            objInfo.mShortcutIcon = getObjectInfo().getShortcutIcon(Type.normal);
            objInfo.mDisplayName = getObjectInfo().mDisplayName;
            objInfo.mSlotType = getObjectInfo().mSlotType;
            final IconBox iconBox = (IconBox) SEHomeUtils.getObjectByClassName(getScene(), objInfo);
            parent.addChild(iconBox, false);
            modelInfo.register(this);
            modelInfo.cloneMenuItemInstance(parent, objInfo.mIndex, false,
                    objInfo.mModelInfo.mStatus);
            iconBox.initStatus(getScene());

            copeStatusTo(iconBox);
            iconBox.setChangedToObj(this);
            iconBox.setBeginLayer(this.getBeginLayer());
            setChangedToObj(iconBox);
        }
        getChangedToObj().setTouch(getTouchX(), getTouchY());
        getChangedToObj().setVisible(true, true);
        getChangedToObj().setPressed(true);
        setVisible(false, true);
        setPressed(false);
        getScene().changeTouchDelegate(getChangedToObj());
        return (IconBox)getChangedToObj();
    }

    public static ShortcutObject create(SE3DScene scene, PreViewObject preViewObject) {
        ItemInfo itemInfo = preViewObject.getItemInfo();
        ObjectInfo info = new ObjectInfo();
        info.mName = generateShortcutName();
        info.mSceneName = scene.mSceneName;
        info.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
        info.mObjectSlot.mSpanX = itemInfo.getSpanX();
        info.mObjectSlot.mSpanY = itemInfo.getSpanY();
        info.mComponentName = itemInfo.getComponentName();
        info.mClassName = ShortcutObject.class.getName();
        info.mType = ModelInfo.Type.SHORTCUT_ICON;
        Context context = SESceneManager.getInstance().getContext();
        ShortcutObject shortcutObject = new ShortcutObject(scene, info.mName, info.mIndex);
        shortcutObject.setIsFresh(true);
        SERect3D rect = null;//new SERect3D(new SEVector3f(1, 0, 0), new SEVector3f(0, 0, 1));
        HouseObject houseObject = ModelInfo.getHouseObject(scene);
        int w = getAppIconWidth(context);//(int) (itemInfo.getSpanX() * houseObject.getWallUnitSizeX());
        int h = w;//(int) (itemInfo.getSpanY() * houseObject.getWallUnitSizeY());
        rect = new SERect3D(w, h);
        //rect.setSize(w, h, 1);
        AppItemInfo.BitmapInfo bitmapInfo = AppItemInfo.getBitmap(itemInfo.getResolveInfo());
        //Bitmap icon = AppItemInfo.getBitmap(itemInfo.getResolveInfo(), w, h);
        String imageName = info.mName + "_imageName";
        String imageKey = info.mName + "_imageKey";
        shortcutObject.setBitmapSize(bitmapInfo.bitmapWidth, bitmapInfo.bitmapHeight, bitmapInfo.bitmapContentWidth, bitmapInfo.bitmapContentHeight);
        SEBitmap bp = new SEBitmap(bitmapInfo.bitmap, SEBitmap.Type.normal);
        shortcutObject.mIconObject = new SEObject(scene, info.mName + "_icon");
        SEObjectFactory.createRectangle(shortcutObject.mIconObject, rect, imageName, imageKey, bp);
        int bitmapW = 128;
        int bitmapH = 128;
        float scale = 1;
        if (bitmapW > 128) {
            scale = 128f / bitmapW;
            bitmapH = (int) (bitmapH * scale);
            bitmapW = 128;

        }
        shortcutObject.mIconObject.setImageSize(bitmapW, bitmapH);
        shortcutObject.setObjectInfo(info);
        shortcutObject.se_setNeedBlendSort_JNI(new float[] {0, 1f, 0});
        info.saveToDB();
        return shortcutObject;
    }

    public ShortcutObject(SE3DScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void onSlotSuccess() {
        if (isFresh()) {
            setIsFresh(false);
            handleSelectShortcut();
        } else {
            super.onSlotSuccess();
            NormalObject parent = (NormalObject)getParent();
            if (ModelInfo.isHouseVesselObject(parent)) {
                showBackgroud();
            } else {
                hideBackgroud();
            }
        }
    }

    private void handleSelectShortcut() {
        getScene().setTouchDelegate(ShortcutObject.this);
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        intent.setComponent(getObjectInfo().mComponentName);
        if (!SESceneManager.getInstance().startActivityForResult(intent, SE3DHomeScene.REQUEST_CODE_SELECT_SHORTCUT)) {
            getScene().removeTouchDelegate();
            handleActivityNotFound();
        }
    }

    private void handleActivityNotFound() {
        ToastUtils.showActivityNotFound();
    }

    public void updateShortcut(Intent data) {
        final String displayName = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Intent shortIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        Bitmap icon = null;
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = (Bitmap) bitmap;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    ShortcutIconResource iconResource = (ShortcutIconResource) extra;
                    Resources resources = getContext().getPackageManager().getResourcesForApplication(
                            iconResource.packageName);
                    int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = BitmapFactory.decodeResource(resources, id);
                } catch (Exception e) {
                }
            }
        }
        getObjectInfo().mShortcutUrl = shortIntent.toURI();
        final Bitmap shortcutIcon = icon;
        LoadResThread.getInstance().process(new Runnable() {
            public void run() {
                ResolveInfo resolveInfo = SEHomeUtils.findResolveInfoByComponent(getContext(),
                        getObjectInfo().mComponentName);
                String label = displayName;
                getObjectInfo().mDisplayName = label;
                getObjectInfo().updateToDB(false);
                if (resolveInfo == null) {
                    return;
                }
                if (TextUtils.isEmpty(label)) {
                    PackageManager pm = getContext().getPackageManager();
                    label = resolveInfo.loadLabel(pm).toString();
                }

                /*
                HouseObject houseObject = ModelInfo.getHouseObject(getScene());
                int w = (int) (getObjectInfo().getSpanX() * houseObject.getWallUnitSizeX());
                int h = (int) (getObjectInfo().getSpanY() * houseObject.getWallUnitSizeY());
                Bitmap bitmapWithText;
                if (shortcutIcon == null) {
                    bitmapWithText = AppItemInfo.getBitmap(resolveInfo, displayName, w, h);
                } else {
                    bitmapWithText = AppItemInfo.getBitmapWithText(shortcutIcon, label, w, h);
                    getObjectInfo().mShortcutIcon = new SEBitmap(shortcutIcon, SEBitmap.Type.normal);
                }
                final int imageData = SEObject.loadImageData_JNI(bitmapWithText);
                if (label != null) {
                    //recycle the new bitmap.
                    bitmapWithText.recycle();
                }
                //will recycle the shortcutIcon.

                new SECommand(getScene()) {
                    public void run() {
                        String imageKey = mName + "_imageKey";
                        SEObject.addImageData_JNI(imageKey, imageData);
                    }
                }.execute();
                */
            }
        });
    }

}
