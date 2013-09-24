package com.borqs.se.object3d;

import com.borqs.se.SEVector.SERotate;
import com.borqs.se.SEVector.SEVector3f;
import com.borqs.se.home3d.ProviderUtils;

public class TransParas {
    private static final int AXIS_NUM = 10;

    public SEVector3f mTranslate;
    public SERotate mRotate;
    public SEVector3f mScale;

    public TransParas() {
        mTranslate = new SEVector3f();
        mRotate = new SERotate();
        mScale = new SEVector3f(1, 1, 1);
    }

    public TransParas(SEVector3f translate, SERotate rotate, SEVector3f scale) {
        mTranslate = translate;
        mRotate = rotate;
        mScale = scale;
    }

    public void set(TransParas transParas) {
        mTranslate = transParas.mTranslate.clone();
        mRotate = transParas.mRotate.clone();
        mScale = transParas.mScale.clone();
    }

    public TransParas clone() {
        TransParas clone = new TransParas(mTranslate.clone(), mRotate.clone(), mScale.clone());
        return clone;
    }

    public void init(float[] data) {
        mTranslate.mD[0] = data[0];
        mTranslate.mD[1] = data[1];
        mTranslate.mD[2] = data[2];
        mRotate.mD[0] = data[3];
        mRotate.mD[1] = data[4];
        mRotate.mD[2] = data[5];
        mRotate.mD[3] = data[6];
        mScale.mD[0] = data[7];
        mScale.mD[1] = data[8];
        mScale.mD[2] = data[9];
    }

    public float[] getFloatArray() {
        float[] array = new float[10];
        array[0] = mTranslate.mD[0];
        array[1] = mTranslate.mD[1];
        array[2] = mTranslate.mD[2];
        array[3] = mRotate.mD[0];
        array[4] = mRotate.mD[1];
        array[5] = mRotate.mD[2];
        array[6] = mRotate.mD[3];
        array[7] = mScale.mD[0];
        array[8] = mScale.mD[1];
        array[9] = mScale.mD[2];
        return array;
    }

    public String toString() {
        return ProviderUtils.floatArrayToString(getFloatArray());
    }

    public float[] getRotate() {
        return mRotate.mD;
    }

    public float[] geScale() {
        return mScale.mD;
    }

    public float[] getTranslate() {
        return mTranslate.mD;
    }

    public static TransParas parseFrom(String str) {
        TransParas transParas = new TransParas();
        transParas.init(ProviderUtils.getFloatArray(str, AXIS_NUM));
        return transParas;
    }
}
