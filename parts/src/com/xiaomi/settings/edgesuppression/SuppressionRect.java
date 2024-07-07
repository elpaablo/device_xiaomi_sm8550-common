/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.edgesuppression;

import java.util.ArrayList;

public class SuppressionRect {
    private int bottomRightX;
    private int bottomRightY;
    private int position;
    private int topLeftX;
    private int topLeftY;
    private int type;
    private ArrayList<Integer> list = new ArrayList<>();
    private int time = 0;
    private int node = 0;

    public void setType(int i) {
        this.type = i;
    }

    public void setPosition(int i) {
        this.position = i;
    }

    public void setTopLeftY(int i) {
        this.topLeftY = i;
    }

    public void setTopLeftX(int i) {
        this.topLeftX = i;
    }

    public void setBottomRightX(int i) {
        this.bottomRightX = i;
    }

    public void setBottomRightY(int i) {
        this.bottomRightY = i;
    }

    public void setValue(int i, int i2, int i3, int i4, int i5, int i6) {
        this.type = i;
        this.position = i2;
        this.topLeftX = i3;
        this.topLeftY = i4;
        this.bottomRightX = i5;
        this.bottomRightY = i6;
        this.time = 0;
        this.node = 0;
    }

    public String toString() {
        return "SuppressionRect{list=" + this.list + ", type=" + this.type + ", position=" + this.position + ", topLeftX=" + this.topLeftX + ", topLeftY=" + this.topLeftY + ", bottomRightX=" + this.bottomRightX + ", bottomRightY=" + this.bottomRightY + ", time=" + this.time + ", node=" + this.node + '}';
    }

    public ArrayList<Integer> getList() {
        if (this.list.size() != 0) {
            this.list.clear();
        }
        this.list.add(Integer.valueOf(this.type));
        this.list.add(Integer.valueOf(this.position));
        this.list.add(Integer.valueOf(this.topLeftX));
        this.list.add(Integer.valueOf(this.topLeftY));
        this.list.add(Integer.valueOf(this.bottomRightX));
        this.list.add(Integer.valueOf(this.bottomRightY));
        this.list.add(Integer.valueOf(this.time));
        this.list.add(Integer.valueOf(this.node));
        return this.list;
    }

    public static class LeftTopHalfRect extends SuppressionRect {
        public LeftTopHalfRect(int i, int i2, int i3) {
            setType(i);
            setPosition(2);
            setTopLeftX(0);
            setTopLeftY(0);
            setBottomRightX(i3);
            setBottomRightY(i2 / 2);
        }
    }

    public static class RightTopHalfRect extends SuppressionRect {
        public RightTopHalfRect(int i, int i2, int i3, int i4) {
            setType(i);
            setPosition(3);
            setTopLeftX(i3 - i4);
            setTopLeftY(0);
            setBottomRightX(i3);
            setBottomRightY(i2 / 2);
        }
    }

    public static class LeftBottomHalfRect extends SuppressionRect {
        public LeftBottomHalfRect(int i, int i2, int i3) {
            setType(i);
            setPosition(2);
            setTopLeftX(0);
            setTopLeftY(i2 / 2);
            setBottomRightX(i3);
            setBottomRightY(i2);
        }
    }

    public static class RightBottomHalfRect extends SuppressionRect {
        public RightBottomHalfRect(int i, int i2, int i3, int i4) {
            setType(i);
            setPosition(3);
            setTopLeftX(i3 - i4);
            setTopLeftY(i2 / 2);
            setBottomRightX(i3);
            setBottomRightY(i2);
        }
    }
}
