package com.borqs.se.object3d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;

import com.borqs.se.LoadResThread;
import com.borqs.se.SEVector.SERect3D;
import com.borqs.se.event.SECommand;
import com.borqs.se.home3d.SEHomeUtils;
import com.borqs.se.object3d.SEObjectData.IMAGE_TYPE;
import com.borqs.se.scene.SE3DScene;

public class SEImageView extends SEObject {
    private float mWidth;
    private float mHeight;
    private int mImageWidth;
    private int mImageHeight;
    private String mImagePath;
    private Bitmap mBitmap;
    private int mImageID = 0;

    private String mImageName;
    private String mImageKey;

    public SEImageView(SE3DScene scene, String name, int index) {
        super(scene, name, index);
    }

    public SEImageView(SE3DScene scene, String name) {
        super(scene, name);
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        if (!SEHomeUtils.isPower2(mImageWidth) || !SEHomeUtils.isPower2(mImageHeight)) {
            throw new IllegalArgumentException("Bitmap size is not right, engine support power2 image only");
        }
        mBitmap = bitmap;
        mImagePath = null;
        changeImage();
    }

    public void setBackground(int id) {
        mImageID = id;
        mImagePath = null;
        mBitmap = null;
        changeImage();
    }

    /**
     * such as "assets/bask/background.png"
     */
    public void setBackgroundImage(String path, int imageW, int imageH) {
        mImageWidth = imageW;
        mImageHeight = imageH;
        if (!SEHomeUtils.isPower2(mImageWidth) || !SEHomeUtils.isPower2(mImageHeight)) {
            throw new IllegalArgumentException("Bitmap size is not right, engine support power2 image only");
        }
        mImagePath = path;
        mBitmap = null;
        changeImage();
    }

    private void changeImage() {
        if (mImageKey == null) {
            return;
        }
        LoadResThread.getInstance().process(new Runnable() {
            public void run() {
                final int imageData;
                if (mImagePath != null) {
                    imageData = SEObject.loadImageData_JNI(mImagePath);
                } else if (mBitmap != null && !mBitmap.isRecycled()) {
                    imageData = SEObject.loadImageData_JNI(mBitmap);
                    mBitmap.recycle();
                } else if (mImageID != 0) {
                    Resources res = getContext().getResources();
                    Bitmap bitmap = BitmapFactory.decodeResource(res, mImageID);
                    if (NinePatch.isNinePatchChunk(bitmap.getNinePatchChunk())) {
                        NinePatch npImage = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);
                        int imageWidth = (int) mWidth;
                        int imageHeight = (int) mHeight;
                        int newImageWidth = SEHomeUtils.higherPower2(imageWidth);
                        int newImageHeight = SEHomeUtils.higherPower2(imageHeight);
                        mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                        int left = (newImageWidth - imageWidth) / 2;
                        int top = (newImageHeight - imageHeight) / 2;
                        Canvas canvas = new Canvas(mBitmap);
                        Rect imageRect = new Rect(left, top, left + imageWidth, top + imageHeight);
                        npImage.draw(canvas, imageRect);
                    } else {
                        int imageWidth = bitmap.getWidth();
                        int imageHeight = bitmap.getHeight();
                        if (!SEHomeUtils.isPower2(imageWidth) || !SEHomeUtils.isPower2(imageHeight)) {
                            int newImageWidth = SEHomeUtils.higherPower2(imageWidth);
                            int newImageHeight = SEHomeUtils.higherPower2(imageHeight);
                            mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                            int left = (newImageWidth - imageWidth) / 2;
                            int top = (newImageHeight - imageHeight) / 2;
                            Canvas canvas = new Canvas(mBitmap);
                            canvas.drawBitmap(bitmap, left, top, null);
                        } else {
                            mBitmap = bitmap;
                        }
                    }
                    imageData = SEObject.loadImageData_JNI(mBitmap);
                    mBitmap.recycle();
                } else {
                    imageData = 0;
                }
                new SECommand(getScene()) {
                    public void run() {
                        SEObject.addImageData_JNI(mImageKey, imageData);
                    }
                }.execute();

            }
        });
    }

    public void setSize(float w, float h) {
        mWidth = w;
        mHeight = h;
        changeImage();
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    @Override
    public void onRender(SECamera camera) {
        SERect3D rect = new SERect3D(mWidth, mHeight);
        mImageName = mName + "_image";
        if (mImagePath != null) {
            mImageKey = mImagePath;
            SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.IMAGE, mImageName, mImagePath, null, null, true);
            setImageSize(mImageWidth, mImageHeight);

        } else if (mBitmap != null) {
            mImageKey = mName + "_image_key";
            SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.BITMAP, mImageName, mImageKey, mBitmap, null, true);
            setImageSize(mImageWidth, mImageHeight);

        } else {
            mImageKey = mName + "_image_key";
            Resources res = getContext().getResources();
            Bitmap bitmap = BitmapFactory.decodeResource(res, mImageID);
            if (NinePatch.isNinePatchChunk(bitmap.getNinePatchChunk())) {
                NinePatch npImage = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);
                int imageWidth = (int) mWidth;
                int imageHeight = (int) mHeight;
                int newImageWidth = SEHomeUtils.higherPower2(imageWidth);
                int newImageHeight = SEHomeUtils.higherPower2(imageHeight);
                mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                int left = (newImageWidth - imageWidth) / 2;
                int top = (newImageHeight - imageHeight) / 2;
                Canvas canvas = new Canvas(mBitmap);
                Rect imageRect = new Rect(left, top, left + imageWidth, top + imageHeight);
                npImage.draw(canvas, imageRect);
                SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.BITMAP, mImageName, mImageKey, mBitmap, null,
                        true);
                setImageSize(imageWidth, imageHeight);
            } else {
                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                if (!SEHomeUtils.isPower2(imageWidth) || !SEHomeUtils.isPower2(imageHeight)) {
                    int newImageWidth = SEHomeUtils.higherPower2(imageWidth);
                    int newImageHeight = SEHomeUtils.higherPower2(imageHeight);
                    mBitmap = Bitmap.createBitmap(newImageWidth, newImageHeight, Bitmap.Config.ARGB_8888);
                    int left = (newImageWidth - imageWidth) / 2;
                    int top = (newImageHeight - imageHeight) / 2;
                    Canvas canvas = new Canvas(mBitmap);
                    canvas.drawBitmap(bitmap, left, top, null);
                } else {
                    mBitmap = bitmap;
                }

                SEObjectFactory.createRectangle(this, rect, IMAGE_TYPE.BITMAP, mImageName, mImageKey, mBitmap, null,
                        true);
                setImageSize(imageWidth, imageHeight);

            }
        }

    }

    @Override
    public void onRenderFinish(SECamera camera) {
        setBlendSortAxis(AXIS.Y);
    }

}
