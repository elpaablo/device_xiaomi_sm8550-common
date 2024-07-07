/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.edgesuppression;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.xiaomi.settings.R;

public class EdgeSuppressionSettings extends CollapsingToolbarBaseActivity {
    int direction;
    private SeekBar edgesize;
    private EdgeSuppressionManager mEdgeSuppressionManager;
    private View mLeftView;
    private View mRightView;
    String modeval;
    private TextView seekbarval;
    float setededgesize;
    float setededgesize2;
    String msg = "Edge : ";
    private FrameLayout.LayoutParams mLeftLayoutParams = new FrameLayout.LayoutParams(-1, -1, 51);
    private FrameLayout.LayoutParams mRightLayoutParams = new FrameLayout.LayoutParams(-1, -1, 53);
    private int mTipAreaWidth = 0;
    private int mTipAreaWidth2 = 0;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.edgemode);
        this.mEdgeSuppressionManager = EdgeSuppressionManager.getInstance(this);
        SharedPreferences sharedPreferences = getSharedPreferences("edgesize", 0);
        final SharedPreferences.Editor edit = sharedPreferences.edit();
        this.direction = getDisplay().getRotation();
        this.seekbarval = (TextView) findViewById(R.id.progressval);
        this.edgesize = (SeekBar) findViewById(R.id.edgesizeset);
        String valueOf = String.valueOf(sharedPreferences.getFloat("edgesize", 0.0f));
        this.modeval = valueOf;
        this.seekbarval.setText(valueOf);
        float f = sharedPreferences.getFloat("edgesize", 0.0f);
        this.setededgesize = f;
        this.edgesize.setProgress((int) (f * 100.0f));
        int suppressionSize = EdgeSuppressionManager.getInstance(this).getSuppressionSize(false, this.setededgesize);
        this.mTipAreaWidth = suppressionSize;
        initTipView(this, suppressionSize);
        this.edgesize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                float f2 = i / 100.0f;
                EdgeSuppressionSettings.this.seekbarval.setText(String.valueOf(f2));
                edit.putFloat("edgesize", f2);
                edit.apply();
                EdgeSuppressionSettings.this.CallforChange();
                EdgeSuppressionSettings.this.valUpdate();
            }
        });
    }

    public void CallforChange() {
        EdgeSuppressionManager.getInstance(getApplicationContext()).handleEdgeModeFeatureDirectionModeChange();
    }

    @Override
    public void onStart() {
        super.onStart();
        EdgeSuppressionManager.getInstance(this).registerlistener();
        valUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        EdgeSuppressionManager.getInstance(this).registerlistener();
        valUpdate();
    }

    @Override 
    public void onPause() {
        super.onPause();
        EdgeSuppressionManager.getInstance(this).unregisterlistener();
    }

    @Override
    public void onStop() {
        super.onStop();
        EdgeSuppressionManager.getInstance(this).unregisterlistener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EdgeSuppressionManager.getInstance(this).unregisterlistener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return false;
        }
        onBackPressed();
        return true;
    }

    public void valUpdate() {
        this.setededgesize2 = getSharedPreferences("edgesize", 0).getFloat("edgesize", 0.0f);
        int suppressionSize = EdgeSuppressionManager.getInstance(this).getSuppressionSize(false, this.setededgesize2);
        this.mTipAreaWidth2 = suppressionSize;
        setRestrictedViewWidth(suppressionSize);
    }

    public void setRestrictedViewWidth(int i) {
        this.mLeftView.getLayoutParams().width = i;
        this.mRightView.getLayoutParams().width = i;
        this.mLeftView.setLayoutParams(this.mLeftLayoutParams);
        this.mRightView.setLayoutParams(this.mRightLayoutParams);
    }

    private void initTipView(Context context, int i) {
        FrameLayout.LayoutParams layoutParams = this.mLeftLayoutParams;
        layoutParams.width = i;
        layoutParams.height = this.mEdgeSuppressionManager.mScreenHeight;
        View view = new View(context);
        this.mLeftView = view;
        view.setBackgroundColor(getResources().getColor(R.color.restricted_tip_area_color, null));
        FrameLayout.LayoutParams layoutParams2 = this.mRightLayoutParams;
        layoutParams2.width = i;
        layoutParams2.height = this.mEdgeSuppressionManager.mScreenHeight;
        View view2 = new View(context);
        this.mRightView = view2;
        view2.setBackgroundColor(getResources().getColor(R.color.restricted_tip_area_color, null));
        ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView();
        viewGroup.addView(this.mLeftView, this.mLeftLayoutParams);
        viewGroup.addView(this.mRightView, this.mRightLayoutParams);
    }
}
