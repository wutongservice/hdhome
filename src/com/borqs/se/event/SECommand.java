package com.borqs.se.event;

import com.borqs.se.scene.SE3DScene;

public abstract class SECommand {
    public long when;
    private SEEventQueue mSEEventQueue;
    private boolean mIsFinished = false;
    private boolean mIsLazy = false;
    private int mID = -1;

    public SECommand(SE3DScene scene) {
        this(scene, -1);
    }

    public SECommand(SE3DScene scene, int id) {
        if (scene != null) {
            when = 0;
            mSEEventQueue = scene.getEventQuene();
            mID = id;
        }
    }
    public int getID() {
        return mID;
    }

    public void stop() {
        mIsFinished = true;
    }

    public boolean isFinish() {
        return mIsFinished;
    }

    public void setIsLazy(boolean isLazy) {
        mIsLazy = isLazy;
    }

    public boolean isLazy() {
        return mIsLazy;
    }

    public void execute() {
        if (mSEEventQueue != null) {
            mSEEventQueue.addEvent(this);
        }
    }

    public void executeRun() {
        run();
        if (!mIsLazy) {
            mIsFinished = true;
        }
    }

    public abstract void run();
}
