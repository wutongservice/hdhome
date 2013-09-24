package com.borqs.se.widget3d;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;

import com.borqs.se.LoadResThread;
import com.borqs.se.event.SECommand;
import com.borqs.se.home3d.SEHomeUtils;
import com.borqs.se.object3d.SEObject;
import com.borqs.se.scene.SE3DScene;
import com.borqs.se.scene.SESceneManager;

public class TableFrame extends NormalObject {
    private SEObject mPhotoObject;
    private int mRequestCode;

    private String mImageName;
    protected String mSaveImagePath;
    private File mSdcardTempFile;
    protected int mWidth;
    protected int mHeight;

    protected int mImageSize;

    public TableFrame(SE3DScene scene, String name, int index) {
        super(scene, name, index);
        setCanChangeBind(false);
        mWidth = 43;
        mHeight = 64;
        mImageSize = 128;
    }

    @Override
    public void initStatus(SE3DScene scene) {
        super.initStatus(scene);
        mRequestCode = (int) System.currentTimeMillis();
        if (mRequestCode < 0) {
            mRequestCode = -mRequestCode;
        }
        mSaveImagePath = SEHomeUtils.PKG_FILES_PATH + scene.mSceneName + mName + mIndex + ".png";
        mSdcardTempFile = SEHomeUtils.getTempImageFile();
        mPhotoObject = new SEObject(scene, getObjectInfo().mModelInfo.mChildNames[0], mIndex);
        mImageName = mPhotoObject.getImageName_JNI();

        setHasInit(true);
    }

    @Override
    public void handOnClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", mWidth);
        intent.putExtra("aspectY", mHeight);
        intent.putExtra("output", Uri.fromFile(mSdcardTempFile));
        intent.putExtra("outputFormat", "JPEG");
        SESceneManager.getInstance().startActivityForResult(intent, mRequestCode);
    }

    @Override
    public void onSlotSuccess() {
        setIsFresh(false);
        super.onSlotSuccess();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == mRequestCode) {
            LoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(mSdcardTempFile.getAbsolutePath(), options);
                    options.inSampleSize = SEHomeUtils.computeSampleSize(options, -1,  mImageSize * mImageSize);
                    options.inJustDecodeBounds = false;
                    Bitmap bm = BitmapFactory.decodeFile(mSdcardTempFile.getAbsolutePath(), options);
                    if (bm == null) {
                        return;
                    }
                    Bitmap des = Bitmap.createBitmap(mImageSize, mImageSize, Bitmap.Config.RGB_565);
                    Rect srcRect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
                    int newW;
                    int newH;
                    if (bm.getWidth() > bm.getHeight()) {
                        newW = mImageSize;
                        newH = bm.getHeight() * mImageSize / bm.getWidth();
                    } else {
                        newH = mImageSize;
                        newW = bm.getWidth() * mImageSize / bm.getHeight();
                    }
                    Rect desRect = new Rect((mImageSize - newW) / 2, (mImageSize - newH) / 2, (mImageSize + newW) / 2,
                            (mImageSize + newH) / 2);
                    Canvas canvas = new Canvas(des);
                    canvas.drawBitmap(bm, srcRect, desRect, null);
                    bm.recycle();
                    SEHomeUtils.saveBitmap(des, mSaveImagePath, Bitmap.CompressFormat.PNG);
                    des.recycle();

                    final int imageData = SEObject.loadImageData_JNI(mSaveImagePath);
                    new SECommand(SESceneManager.getInstance().getCurrentScene()) {
                        public void run() {
                            SEObject.applyImage_JNI(mImageName, mSaveImagePath);
                            SEObject.addImageData_JNI(mSaveImagePath, imageData);
                        }
                    }.execute();
                    System.gc();
                }
            });

        }
    }

}
