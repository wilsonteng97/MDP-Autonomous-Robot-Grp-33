package com.example.mdp;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class Robot extends Moveable{
    //Properties for robot
    boolean isDrawn;
    int dir;
    boolean isOn;
    Rect pos;

    public Robot (boolean isDrawn, int dir, boolean isOn) {
        this.isDrawn = isDrawn;
        this.dir = dir;
        this.isOn = isOn;
        this.x = 0;
        this.y = 19;
    }
}
