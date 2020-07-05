package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoxDrawingView extends View {
    private static final String TAG = "BoxDrawingView";

    private static final String KEY_BOXEN = "boxen";
    private static final String KEY_PARENT_VIEW = "parent_view";

    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    private Pointer mPrimaryPointer;
    private Pointer mSecondaryPointer;

    private List<Box> mBoxen = new ArrayList<>();
    private Box mCurrentBox;
    private float mCurrentRotation = 0; // Additional rotation held by user

    public BoxDrawingView(Context context) {
        this(context, null);
    }

    public BoxDrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KEY_BOXEN, (ArrayList<Box>) mBoxen);
        bundle.putParcelable(KEY_PARENT_VIEW, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        mBoxen = bundle.getParcelableArrayList(KEY_BOXEN);
        super.onRestoreInstanceState(bundle.getParcelable(KEY_PARENT_VIEW));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);

        for(Box box : mBoxen){
            // Save canvas, then rotate it before drawing
            canvas.save();
            canvas.rotate(box.getRotation() + mCurrentRotation, getWidth()/2f, getHeight()/2f);

            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.restore(); // Restore canvas to non-rotated state
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        String action = "";

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: // First finger has entered the game
                action = "ACTION_DOWN";
                mPrimaryPointer = new Pointer(event);
                mCurrentBox = new Box(mPrimaryPointer.getStartPos());
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // Second finger has entered the game
                action = "ACTION_POINTER_DOWN";
                mSecondaryPointer = new Pointer(event);
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_UP: // First finger has left the game
                action = "ACTION_UP";
                mPrimaryPointer = null;
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_POINTER_UP: // Second finger has left the game
                action = "ACTION_POINTER_UP";
                for(Box box : mBoxen) box.addRotation(mCurrentRotation);
                mSecondaryPointer = null;
                mCurrentRotation = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if(mCurrentBox != null && mPrimaryPointer != null) {
                    mCurrentBox.setCurrent(mPrimaryPointer.getCurrentPos(event));
                } else if (mPrimaryPointer != null && mSecondaryPointer != null) {
                    mCurrentRotation = angleBetweenTwoLines(
                            mPrimaryPointer.getStartPos(),
                            mSecondaryPointer.getStartPos(),
                            mPrimaryPointer.getCurrentPos(event),
                            mSecondaryPointer.getCurrentPos(event));
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mPrimaryPointer = null;
                mSecondaryPointer = null;
                mCurrentBox = null;
                mCurrentRotation = 0;
                break;
        }

        Log.i(TAG, action
                + " at x=" + event.getX(event.getActionIndex())
                + ", y=" + event.getY(event.getActionIndex()));

        return true;
    }
 
    static float angleBetweenTwoLines(PointF primStart, PointF secondStart, PointF primEnd, PointF secondEnd){
        double rotationStart = Math.atan2(primStart.y - secondStart.y, primStart.x - secondStart.x);
        double rotationEnd = Math.atan2(primEnd.y - secondEnd.y, primEnd.x - secondEnd.x);
        return (float) Math.toDegrees(rotationStart-rotationEnd);
    }
}
