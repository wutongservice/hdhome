package com.borqs.se.thumbnails;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

import com.borqs.se.home3d.SEHomeUtils;
import com.borqs.se.scene.SESceneManager;

public class VideoThumbnailsService {

    public static int CREATE_THUMBNAILS = 0;
    private ContentResolver mResolver;
    private MediaDBObserver mObserver;
    private Task mTask;
    private Bitmap mPlayBitmap;
    private Context mContext;
    
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CREATE_THUMBNAILS) {
                if (mTask != null) {
                    mTask.mStop = true;
                }
                mTask = new Task();
                mTask.start();
            }
        }
    };

    public void start(Context context) {
        mContext = context;
        removeOldDir();
        createSaveDirectory();
        mHandler.sendEmptyMessageDelayed(CREATE_THUMBNAILS, 10000);
        mResolver = mContext.getContentResolver();
        if (mObserver == null) {
            HandlerThread mediaDBObserverThread = new HandlerThread("MediaDBObserverThread");
            mediaDBObserverThread.start();
            Handler handler = new Handler(mediaDBObserverThread.getLooper());
            mObserver = new MediaDBObserver(handler, mContext);
        }
        mObserver.startObserving();
    }

    private class Task extends Thread {
        public boolean mStop = false;
        private String[] mPreListFiles;

        @Override
        public void run() {
            SESceneManager.getInstance().debugOutput("VideoThumbnailsService task begin.");
            if (mStop) {
                return;
            }
            mPreListFiles = getThumbNameList();
            try {
                getAndSaveVideoThumbnail(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            } catch (Exception e) {
                Log.e(SEHomeUtils.TAG, "create video thumb failed : " + e.getMessage());
            }
            SESceneManager.getInstance().debugOutput("VideoThumbnailsService task end.");
        }

        public void getAndSaveVideoThumbnail(Uri uri) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Cursor cursor = mResolver.query(uri,
                    new String[] { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA }, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return;
            }
            if (mPlayBitmap == null) {
                mPlayBitmap = getPlayBitmap();
            }
            int index = 0;
            while (cursor.moveToNext() && !mStop) {
                boolean shoudBreak = false;
                String pathA;
                String pathB;
                long idB;
                pathA = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                if (cursor.moveToNext()) {
                    idB = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    pathB = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    cursor.moveToPrevious();
                } else {
                    cursor.moveToFirst();
                    idB = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    pathB = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    shoudBreak = true;
                }
                String imgName = String.valueOf(idB);
                if (mPreListFiles != null && mPreListFiles.length > index) {
                    if (mPreListFiles[index].equals(imgName)) {
                        index++;
                        if (shoudBreak) {
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        for (int i = index; i < mPreListFiles.length; i++) {
                            File file = new File(SEHomeUtils.THUMB_PATH + "/" + mPreListFiles[i]);
                            file.delete();
                        }
                        mPreListFiles = null;
                    }
                }
                Bitmap bitmapA = ThumbnailUtils.createVideoThumbnail(pathA, Images.Thumbnails.MINI_KIND);
                Bitmap bitmapB = ThumbnailUtils.createVideoThumbnail(pathB, Images.Thumbnails.MINI_KIND);
                createBitmap(bitmapA, bitmapB, imgName);
                index++;
                if (shoudBreak) {
                    break;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        private void createBitmap(Bitmap preBitmap, Bitmap bitmap, String name) {
            Bitmap des = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(des);
            int w;
            int h;
            int newW;
            int newH;
            int left;
            int top;
            Rect srcR;
            Rect desR;
            if (preBitmap != null) {
                w = preBitmap.getWidth();
                h = preBitmap.getHeight();
                srcR = new Rect(0, 0, w, h);
                if (w > 2 * h) {
                    newW = 256;
                    newH = h * 256 / w;
                } else {
                    newH = 128;
                    newW = w * 128 / h;
                }
                left = (256 - newW) / 2;
                top = (128 - newH) / 2;
                desR = new Rect(left, top, left + newW, top + newH);
                canvas.drawBitmap(preBitmap, srcR, desR, null);
            }
            if (bitmap != null) {
                w = bitmap.getWidth();
                h = bitmap.getHeight();
                srcR = new Rect(0, 0, w, h);
                if (w > 2 * h) {
                    newW = 256;
                    newH = h * 256 / w;
                } else {
                    newH = 128;
                    newW = w * 128 / h;
                }
                left = (256 - newW) / 2;
                top = 128 + (128 - newH) / 2;
                desR = new Rect(left, top, left + newW, top + newH);
                canvas.drawBitmap(bitmap, srcR, desR, null);
            }
            if (mPlayBitmap != null) {
                canvas.drawBitmap(mPlayBitmap, 112, 48, null);
                canvas.drawBitmap(mPlayBitmap, 112, 176, null);
            }
            saveBitmapToSdcard(des, name);
        }

    }

    private String[] getThumbNameList() {
        File dir = new File(SEHomeUtils.THUMB_PATH);
        if (dir.exists() && dir.isDirectory()) {
            return dir.list();
        }
        return null;
    }

    public void stopObserving() {
        Log.i(SEHomeUtils.TAG, "Thumb service has been destroyed.");
        if (mObserver != null) {
            mObserver.stopObserving();
        }
    }

    class MediaDBObserver extends ContentObserver {

        private Context mContext;

        MediaDBObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        void startObserving() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false, this);
            resolver.registerContentObserver(MediaStore.Video.Media.INTERNAL_CONTENT_URI, false, this);
        }

        void stopObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.removeMessages(CREATE_THUMBNAILS);
            mHandler.sendEmptyMessageDelayed(CREATE_THUMBNAILS, 10000);
        }
    }

    private void createSaveDirectory() {
        File file = new File(SEHomeUtils.SDCARD_PATH);
        if (file.exists()) {
            if (!file.isDirectory()) {
                file.delete();
                file.mkdir();
            }
        } else {
            file.mkdir();
        }
        file = new File(SEHomeUtils.THUMB_PATH);
        if (!file.exists()) {
            file.mkdir();
        } else if (!file.isDirectory()) {
            file.delete();
            file.mkdir();
        }
    }

    private void saveBitmapToSdcard(Bitmap bitmap, String name) {
        File f = new File(SEHomeUtils.THUMB_PATH + "/" + name);
        try {
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getPlayBitmap() {
        try {
            InputStream is = mContext.getAssets().open("base/tv/tvplay.png");
            Bitmap temp = BitmapFactory.decodeStream(is);
            is.close();
            return temp;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void removeOldDir() {
        File file = new File(SEHomeUtils.THUMB_PATH);
        if (file.exists()) {
            deleteFileAndFolder(file.getAbsolutePath());
        }
    }
    
    private void deleteFileAndFolder(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : file.listFiles()) {
                    if (f.isDirectory()) {
                        deleteFileAndFolder(f.getAbsolutePath());
                    } else {
                        f.delete();
                    }
                }
            }
        }
        file.delete();
    }
}
