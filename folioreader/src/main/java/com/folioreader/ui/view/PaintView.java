package com.folioreader.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.folioreader.util.Draw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PaintView extends View {

    public static int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;

    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Bitmap curBit = null;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    private ArrayList<Draw> paths = new ArrayList<>();
    private ArrayList<Draw> undo = new ArrayList<>();

    public PaintView(Context context) {
        super(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
    }

    public void initialise(DisplayMetrics displayMetrics, String bitmap) {
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        if (bitmap != null) {
            Bitmap bit = BitmapFactory.decodeFile(bitmap);
            curBit = bit;
            backgroundColor = DEFAULT_BG_COLOR;
            mCanvas.drawColor(backgroundColor);
            mCanvas.drawBitmap(bit, 0, 0, null);
        } else {
            backgroundColor = DEFAULT_BG_COLOR;
            mCanvas.drawColor(backgroundColor);
        }

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    @Override

    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor); // WRONG
        if (curBit != null)
            mCanvas.drawBitmap(curBit, 0, 0, null);

        for (Draw draw : paths) {

            mPaint.setColor(draw.color); // WRONG
            mPaint.setStrokeWidth(draw.strokeWidth);
            mPaint.setMaskFilter(null);

            mCanvas.drawPath(draw.path, mPaint);

        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        mPath = new Path();

        Draw draw = new Draw(currentColor, strokeWidth, mPath);
        paths.add(draw);

        mPath.reset();
        mPath.moveTo(x, y);

        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        curBit = Bitmap.createBitmap(mCanvas.getWidth(), mCanvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas emptyCanvas = new Canvas(curBit);
        emptyCanvas.drawColor(DEFAULT_BG_COLOR);
        emptyCanvas.drawBitmap(curBit, 0, 0, mBitmapPaint);
        backgroundColor = DEFAULT_BG_COLOR;
        mCanvas.drawColor(DEFAULT_BG_COLOR);
        mCanvas.save();
        paths.clear();
        invalidate();
    }

    public void undo() {
        if (paths.size() > 0) {
            undo.add(paths.remove(paths.size() - 1));
            invalidate(); // add
        } else {
            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_LONG).show();
        }
    }

    public void redo() {
        if (undo.size() > 0) {
            paths.add(undo.remove(undo.size() - 1));
            invalidate(); // add
        } else {
            Toast.makeText(getContext(), "Nothing to redo", Toast.LENGTH_LONG).show();
        }
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public void setColor(int color) {
        currentColor = color;
    }

    public void saveImage() {
        String mPath = "";
        Bitmap emptyBitmap = Bitmap.createBitmap(mCanvas.getWidth(), mCanvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas emptyCanvas = new Canvas(emptyBitmap);
        emptyCanvas.drawColor(DEFAULT_BG_COLOR);
        emptyCanvas.drawBitmap(emptyBitmap, 0, 0, mBitmapPaint);
        if (!mBitmap.sameAs(emptyBitmap)){
            mPath = getActivity().getApplicationContext().getExternalFilesDir(null) + "/epubviewer/draw.jpg";
            File image = new File(mPath);
            if (!image.exists())
                image.getParentFile().mkdir();
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(image);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
        Intent intent = new Intent();
        intent.putExtra("bitmap", mPath);
        Activity activity = getActivity();
        if (activity != null) {
            activity.setResult(100, intent);
            activity.finish();
        }
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
