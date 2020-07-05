package com.bignerdranch.android.draganddraw;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Class encapsulating information related to a current touch event by a pointer.
 */
public class Pointer {

    private PointF startPos;
    private int pointerIndex;

    public Pointer(MotionEvent event) {
        pointerIndex = event.getActionIndex();
        startPos = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
    }

    public PointF getStartPos() {
        return startPos;
    }

    public float getCurrentX(MotionEvent event) {
        return event.getX(event.findPointerIndex(pointerIndex));
    }

    public float getCurrentY(MotionEvent event) {
        return event.getY(event.findPointerIndex(pointerIndex));
    }

    public PointF getCurrentPos(MotionEvent event) {
        return new PointF(getCurrentX(event), getCurrentY(event));
    }
}
