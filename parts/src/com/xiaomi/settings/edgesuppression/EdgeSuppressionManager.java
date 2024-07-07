/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.edgesuppression;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Iterator;

import com.xiaomi.settings.R;
import com.xiaomi.settings.touch.TfWrapper;

public class EdgeSuppressionManager {
    public static boolean mIsSupportEdgeMode = true;
    private static EdgeSuppressionManager sInstance;
    private int[] mAbsoluteLevel;
    private float mAllowAdjustSize;
    private int[] mConner;
    private Context mContext;
    private float mDefaultSize;
    private float mEdgeModeSize;
    SharedPreferences mEdgeSizeSet;
    private int mIndex;
    private boolean mIsHorizontal;
    private int mMaxAdjustValue;
    private int mMaxShrinkSize;
    private int mMinShrinkSize;
    private int[] mOther;
    private ArrayList<SuppressionRect> mRectList;
    public int mScreenHeight;
    public int mScreenWidth;
    private ArrayList<Integer> mSendList;
    private float[] mConditionLevel = new float[5];
    private String mEdgeModeType = "default_suppression";
    private int mHoldSensorState = -1;
    private int mLaysensorState = -1;
    private boolean mSupportSensor = false;
    private boolean mSupporthighResolution = false;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        }
    };

    public enum mMode {
        CORNER(0),
        CONDITION(1),
        ABSOLUTE(2);

        private int index;

        mMode(int i) {
            this.index = i;
        }
    }

    private EdgeSuppressionManager(Context context) {
        this.mEdgeSizeSet = context.getSharedPreferences("edgesize", 0);
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        this.mScreenWidth = Math.min(metrics.widthPixels - 1, metrics.heightPixels - 1);
        this.mScreenHeight = Math.max(metrics.widthPixels - 1, metrics.heightPixels - 1);
        this.mContext = context;
        resetIndex();
        initParam();
    }

    public static EdgeSuppressionManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (EdgeSuppressionManager.class) {
                if (sInstance == null) {
                    sInstance = new EdgeSuppressionManager(context);
                }
            }
        }
        return sInstance;
    }

    public void registerlistener() {
        PreferenceManager.getDefaultSharedPreferences(this.mContext).registerOnSharedPreferenceChangeListener(this.mListener);
    }

    public void unregisterlistener() {
        PreferenceManager.getDefaultSharedPreferences(this.mContext).unregisterOnSharedPreferenceChangeListener(this.mListener);
    }

    public ArrayList<Integer> handleEdgeModeFeatureDirectionModeChange() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        int rotation = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRotation();
        arrayList.add(0);
        if (!mIsSupportEdgeMode) {
            return arrayList;
        }
        float f = this.mEdgeSizeSet.getFloat("edgesize", 0.0f);
        this.mEdgeModeSize = f;
        ArrayList<Integer> suppressionRect = getSuppressionRect(rotation, f);
        TfWrapper.setTouchFeature(
                new TfWrapper.TfParams(15, suppressionRect));
        return suppressionRect;
    }

    private void initParam() {
        this.mSupporthighResolution = false;
        this.mSupportSensor = false;
        this.mMaxAdjustValue = 0;
        this.mEdgeModeSize = 0.0f;
        int[] intArray = this.mContext.getResources().getIntArray(R.array.edge_suppresson_condition);
        float f = this.mSupporthighResolution ? 100.0f : 10.0f;
        if (intArray != null && intArray.length >= 6) {
            float[] fArr = this.mConditionLevel;
            fArr[0] = intArray[1] / f;
            fArr[1] = intArray[2] / f;
            fArr[2] = intArray[3] / f;
            fArr[3] = intArray[4] / f;
            fArr[4] = intArray[5] / f;
            this.mMaxAdjustValue = intArray[0];
        }
        float[] fArr2 = this.mConditionLevel;
        this.mAllowAdjustSize = fArr2[4] - fArr2[0];
        this.mDefaultSize = fArr2[2];
        int[] intArray2 = this.mContext.getResources().getIntArray(R.array.edge_suppresson_absolute);
        this.mAbsoluteLevel = intArray2;
        if (intArray2 == null || intArray2.length < 5) {
            this.mAbsoluteLevel = new int[]{0, 0, 0, 0, 0};
        }
        int[] intArray3 = this.mContext.getResources().getIntArray(R.array.edge_suppresson_corner);
        this.mConner = intArray3;
        if (intArray3 == null || intArray3.length < 4) {
            this.mConner = new int[]{0, 0, 0, 0};
        }
        int[] intArray4 = this.mContext.getResources().getIntArray(R.array.edge_suppresson_size);
        this.mOther = intArray4;
        if (intArray4 == null || intArray4.length < 4) {
            this.mOther = new int[]{12, 96, 45, 10};
        }
        this.mRectList = new ArrayList<>(this.mOther[0]);
        this.mSendList = new ArrayList<>(this.mOther[1]);
        int[] iArr = this.mOther;
        this.mMaxShrinkSize = iArr[2];
        this.mMinShrinkSize = iArr[3];
    }

    private ArrayList<Integer> getSuppressionRect(int i, float f) {
        if (this.mRectList.isEmpty()) {
            initArrayList();
        }
        this.mIsHorizontal = false;
        if (i == 1 || i == 3) {
            this.mIsHorizontal = true;
        }
        this.mSendList.clear();
        if (this.mIsHorizontal) {
            setRectPointForHorizontal(getSuppressionSize(true, f), mMode.ABSOLUTE.index);
            setRectPointForHorizontal(getSuppressionSize(false, f), mMode.CONDITION.index);
        } else {
            setRectPointForPortrait(getSuppressionSize(true, f), mMode.ABSOLUTE.index);
            setRectPointForPortrait(getSuppressionSize(false, f), mMode.CONDITION.index);
        }
        setCornerRectPoint(i);
        getArrayList(this.mRectList);
        return this.mSendList;
    }

    private void setRectPointForHorizontal(int i, int i2) {
        int i3 = this.mHoldSensorState;
        if (i3 != -1 && i3 != 0) {
            if (i3 == 1) {
                setRectValue(getSuppressionRectNum(), i2, 0, 0, 0, this.mScreenWidth, i);
                setRectValue(getSuppressionRectNum(), i2, 1, 0, 0, 0, 0);
                ArrayList<SuppressionRect> arrayList = this.mRectList;
                int i4 = this.mIndex;
                this.mIndex = i4 + 1;
                arrayList.set(i4, new SuppressionRect.LeftTopHalfRect(i2, this.mScreenHeight, i));
                ArrayList<SuppressionRect> arrayList2 = this.mRectList;
                int i5 = this.mIndex;
                this.mIndex = i5 + 1;
                arrayList2.set(i5, new SuppressionRect.RightTopHalfRect(i2, this.mScreenHeight, this.mScreenWidth, i));
                return;
            }
            if (i3 == 2) {
                setRectValue(getSuppressionRectNum(), i2, 0, 0, 0, 0, 0);
                SuppressionRect suppressionRectNum = getSuppressionRectNum();
                int i6 = this.mScreenHeight;
                setRectValue(suppressionRectNum, i2, 1, 0, i6 - i, this.mScreenWidth, i6);
                ArrayList<SuppressionRect> arrayList3 = this.mRectList;
                int i7 = this.mIndex;
                this.mIndex = i7 + 1;
                arrayList3.set(i7, new SuppressionRect.LeftBottomHalfRect(i2, this.mScreenHeight, i));
                ArrayList<SuppressionRect> arrayList4 = this.mRectList;
                int i8 = this.mIndex;
                this.mIndex = i8 + 1;
                arrayList4.set(i8, new SuppressionRect.RightBottomHalfRect(i2, this.mScreenHeight, this.mScreenWidth, i));
                return;
            }
            if (i3 != 3) {
                return;
            }
        }
        setRectPointForHorizontalWithoutSensor(i, i2);
    }

    private void setRectPointForHorizontalWithoutSensor(int i, int i2) {
        setRectValue(getSuppressionRectNum(), i2, 0, 0, 0, this.mScreenWidth, i);
        SuppressionRect suppressionRectNum = getSuppressionRectNum();
        int i3 = this.mScreenHeight;
        setRectValue(suppressionRectNum, i2, 1, 0, i3 - i, this.mScreenWidth, i3);
        setRectValue(getSuppressionRectNum(), i2, 2, 0, 0, i, this.mScreenHeight);
        SuppressionRect suppressionRectNum2 = getSuppressionRectNum();
        int i4 = this.mScreenWidth;
        setRectValue(suppressionRectNum2, i2, 3, i4 - i, 0, i4, this.mScreenHeight);
    }

    private void setRectPointForPortrait(int i, int i2) {
        setRectValue(getSuppressionRectNum(), i2, 0, 0, 0, 0, 0);
        setRectValue(getSuppressionRectNum(), i2, 1, 0, 0, 0, 0);
        if (mMode.ABSOLUTE.index == i2) {
            setRectValue(getSuppressionRectNum(), i2, 2, 0, 0, i, this.mScreenHeight);
            SuppressionRect suppressionRectNum = getSuppressionRectNum();
            int i3 = this.mScreenWidth;
            setRectValue(suppressionRectNum, i2, 3, i3 - i, 0, i3, this.mScreenHeight);
            return;
        }
        if (mMode.CONDITION.index != i2) {
            return;
        }
        if (this.mHoldSensorState != 2) {
            setRectValue(getSuppressionRectNum(), i2, 2, 0, 0, i, this.mScreenHeight);
            SuppressionRect suppressionRectNum2 = getSuppressionRectNum();
            int i4 = this.mScreenWidth;
            setRectValue(suppressionRectNum2, i2, 3, i4 - i, 0, i4, this.mScreenHeight);
            return;
        }
        ArrayList<SuppressionRect> arrayList = this.mRectList;
        int i5 = this.mIndex;
        this.mIndex = i5 + 1;
        arrayList.set(i5, new SuppressionRect.LeftBottomHalfRect(i2, this.mScreenHeight, i));
        ArrayList<SuppressionRect> arrayList2 = this.mRectList;
        int i6 = this.mIndex;
        this.mIndex = i6 + 1;
        arrayList2.set(i6, new SuppressionRect.RightBottomHalfRect(i2, this.mScreenHeight, this.mScreenWidth, i));
    }

    private void setCornerRectPoint(int i) {
        int i2;
        int i3;
        int i4 = mMode.CORNER.index;
        if (this.mIsHorizontal) {
            int[] iArr = this.mConner;
            i2 = iArr[2];
            i3 = iArr[3];
        } else {
            int[] iArr2 = this.mConner;
            i2 = iArr2[0];
            i3 = iArr2[1];
        }
        int i5 = i3;
        int i6 = i2;
        if (i == 0) {
            setRectValue(getSuppressionRectNum(), i4, 0, 0, 0, 0, 0);
            setRectValue(getSuppressionRectNum(), i4, 1, 0, 0, 0, 0);
            SuppressionRect suppressionRectNum = getSuppressionRectNum();
            int i7 = this.mScreenHeight;
            setRectValue(suppressionRectNum, i4, 2, 0, i7 - i5, i6, i7);
            SuppressionRect suppressionRectNum2 = getSuppressionRectNum();
            int i8 = this.mScreenWidth;
            int i9 = this.mScreenHeight;
            setRectValue(suppressionRectNum2, i4, 3, i8 - i6, i9 - i5, i8, i9);
            return;
        }
        if (i == 1) {
            setRectValue(getSuppressionRectNum(), i4, 0, 0, 0, i6, i5);
            setRectValue(getSuppressionRectNum(), i4, 1, 0, 0, 0, 0);
            SuppressionRect suppressionRectNum3 = getSuppressionRectNum();
            int i10 = this.mScreenHeight;
            setRectValue(suppressionRectNum3, i4, 2, 0, i10 - i5, i6, i10);
            setRectValue(getSuppressionRectNum(), i4, 3, 0, 0, 0, 0);
            return;
        }
        if (i == 2) {
            setRectValue(getSuppressionRectNum(), i4, 0, 0, 0, i6, i5);
            SuppressionRect suppressionRectNum4 = getSuppressionRectNum();
            int i11 = this.mScreenWidth;
            setRectValue(suppressionRectNum4, i4, 1, i11 - i6, 0, i11, i5);
            setRectValue(getSuppressionRectNum(), i4, 2, 0, 0, 0, 0);
            setRectValue(getSuppressionRectNum(), i4, 3, 0, 0, 0, 0);
            return;
        }
        if (i == 3) {
            setRectValue(getSuppressionRectNum(), i4, 0, 0, 0, 0, 0);
            SuppressionRect suppressionRectNum5 = getSuppressionRectNum();
            int i12 = this.mScreenWidth;
            setRectValue(suppressionRectNum5, i4, 1, i12 - i6, 0, i12, i5);
            setRectValue(getSuppressionRectNum(), i4, 2, 0, 0, 0, 0);
            SuppressionRect suppressionRectNum6 = getSuppressionRectNum();
            int i13 = this.mScreenWidth;
            int i14 = this.mScreenHeight;
            setRectValue(suppressionRectNum6, i4, 3, i13 - i6, i14 - i5, i13, i14);
        }
    }

    public int getSuppressionSize(boolean z, float f) {
        int i;
        int i2 = this.mAbsoluteLevel[0];
        float suppressionSizeWithLaySensor = getSuppressionSizeWithLaySensor(f);
        if (!z) {
            i2 = (int) (this.mMaxAdjustValue * suppressionSizeWithLaySensor);
        } else if (this.mSupporthighResolution) {
            float[] fArr = this.mConditionLevel;
            if (suppressionSizeWithLaySensor == fArr[1]) {
                i = this.mAbsoluteLevel[1];
            } else if (suppressionSizeWithLaySensor == fArr[2]) {
                i = this.mAbsoluteLevel[2];
            } else {
                i = suppressionSizeWithLaySensor != 0.0f ? (int) ((this.mAbsoluteLevel[4] * (suppressionSizeWithLaySensor - fArr[0])) / this.mAllowAdjustSize) : 0;
            }
            i2 = i;
        } else if (Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[0]) == 1 && Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[1]) != 1) {
            i2 = this.mAbsoluteLevel[1];
        } else if (Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[1]) == 1 && Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[2]) != 1) {
            i2 = this.mAbsoluteLevel[2];
        } else if (Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[2]) == 1 && Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[3]) != 1) {
            i2 = this.mAbsoluteLevel[3];
        } else if (Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[3]) == 1 && Float.compare(suppressionSizeWithLaySensor, this.mConditionLevel[4]) != 1) {
            i2 = this.mAbsoluteLevel[4];
        }
        Slog.i("EdgeSuppressionManager", "isAbsolute = " + z + ", result = " + i2);
        return i2;
    }

    private void initArrayList() {
        for (int i = 0; i < this.mOther[0]; i++) {
            this.mRectList.add(new SuppressionRect());
        }
    }

    private void setRectValue(SuppressionRect suppressionRect, int i, int i2, int i3, int i4, int i5, int i6) {
        suppressionRect.setValue(i, i2, i3, i4, i5, i6);
    }

    private void getArrayList(ArrayList<SuppressionRect> arrayList) {
        Iterator<SuppressionRect> it = arrayList.iterator();
        while (it.hasNext()) {
            this.mSendList.addAll(it.next().getList());
        }
        resetIndex();
    }

    private void resetIndex() {
        this.mIndex = 0;
    }

    private SuppressionRect getSuppressionRectNum() {
        ArrayList<SuppressionRect> arrayList = this.mRectList;
        int i = this.mIndex;
        this.mIndex = i + 1;
        return arrayList.get(i);
    }

    private float getSuppressionSizeWithLaySensor(float f) {
        int i = this.mLaysensorState;
        if (i == -1) {
            return f;
        }
        if (i == 0) {
            return this.mConditionLevel[2];
        }
        if (i == 1) {
            return this.mConditionLevel[3];
        }
        if (i != 2) {
            return f;
        }
        return 0.0f;
    }
}
