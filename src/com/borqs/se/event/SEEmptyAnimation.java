package com.borqs.se.event;

import android.view.animation.Interpolator;

import com.borqs.se.LoadResThread;
import com.borqs.se.scene.SE3DScene;
import com.borqs.se.scene.SESceneManager;

public class SEEmptyAnimation extends SECommand {
    private float mFrom;
    private float mTo;
    private Interpolator mInterpolator;

    private long mPreTime;
    private long mHasRunTime;

    private int mCount = 0;
    private int mAnimationTimes = Integer.MAX_VALUE;
    private long mAnimationTime = Long.MAX_VALUE;
    private SEAnimFinishListener mListener;
    private boolean mIsFinished = false;
    private boolean mHasBeenExecuted = false;
    private boolean mHasBeenRun = false;
    private Runnable mExecuteTask = new Runnable() {
        public void run() {
            execute();
        }
    };

    public SEEmptyAnimation(SE3DScene scene, float from, float to, int times) {
        super(scene);
        mFrom = from;
        mTo = to;
        mHasRunTime = 0;
        if (times != mAnimationTimes) {
            setAnimationTimes(times);
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setAnimationTimes(int times) {
        mAnimationTimes = times;
        if (mAnimationTimes == 0) {
            mAnimationTimes = 1;
        }
        mAnimationTime = mAnimationTimes * 20;
    }

    public void setAnimFinishListener(SEAnimFinishListener listener) {
        mListener = listener;
    }

    public SEAnimFinishListener getAnimFinishListener() {
        return mListener;
    }

    @Override
    public boolean isFinish() {
        return mIsFinished;
    }

    @Override
    public void stop() {
        LoadResThread.getInstance().cancel(mExecuteTask);
        if (!isFinish()) {
            mIsFinished = true;
            mCount = 0;
            onFinish();
        }
    }

    @Override
    public void execute() {
        if (mCount == 0) {
            mHasBeenExecuted = true;
            mIsFinished = false;
            super.execute();
        }
    }

    public boolean hasBeenExecuted() {
        return mHasBeenExecuted;
    }

    public void executeDelayed(long delay) {
        LoadResThread.getInstance().cancel(mExecuteTask);
        LoadResThread.getInstance().process(mExecuteTask, delay);
    }

    public void onFinish() {

    }

    public int getAnimationTimes() {
        return mAnimationTimes;
    }

    public long getAnimationTime() {
        return mAnimationTime;
    }

    public void onBegin() {

    }

    public void run() {
        if (!isFinish()) {
            if (!mHasBeenRun) {
                mHasBeenRun = true;
                if (SESceneManager.USING_TIME_ANIMATION) {
                    mPreTime = System.currentTimeMillis();
                }
                onBegin();
            }
            long usingTime;
            if (SESceneManager.USING_TIME_ANIMATION) {
                long currentTime = System.currentTimeMillis();
                usingTime = currentTime - mPreTime;
                mPreTime = currentTime;
                if (usingTime > 100) {
                    usingTime = 100;
                }
            } else {
                usingTime = SESceneManager.TIME_ONE_FRAME;
            }
            mHasRunTime += usingTime;
            if (mHasRunTime > mAnimationTime) {
                mHasRunTime = mAnimationTime;
            }

            if (!isFinish()) {
                float interpolatorValue = (float) mHasRunTime / (float) mAnimationTime;
                if (mInterpolator != null) {
                    interpolatorValue = mInterpolator.getInterpolation(interpolatorValue);
                }
                float distance = mTo - mFrom;
                float value = mFrom + distance * interpolatorValue;
                onAnimationRun(value);
            }
            if (mHasRunTime >= mAnimationTime) {
                stop();
            }
            if (isFinish() && mListener != null) {
                mListener.onAnimationfinish();
            }
        }
    }

    public void onAnimationRun(float value) {

    }
}
