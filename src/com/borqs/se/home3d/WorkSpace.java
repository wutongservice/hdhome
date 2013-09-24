package com.borqs.se.home3d;

import com.borqs.se.scene.SE3DScene;
import com.borqs.se.scene.SESceneManager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class WorkSpace extends FrameLayout {

    private ScaleGestureDetector mScaleDetector;

    public WorkSpace(Context context) {
        super(context);
    }

    public WorkSpace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WorkSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScaleListener(ScaleGestureDetector.OnScaleGestureListener l) {
        mScaleDetector = new ScaleGestureDetector(getContext(), l);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getScene() != null && getScene().getStatus(SE3DScene.STATUS_ON_SCALL)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1 && mScaleDetector != null) {
            mScaleDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private SE3DScene getScene() {
        return SESceneManager.getInstance().getCurrentScene();
    }

}
