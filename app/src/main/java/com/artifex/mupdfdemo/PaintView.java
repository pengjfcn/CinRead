package com.artifex.mupdfdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.cinread.ebook.utils.LogUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @Project: mupdfdemo
 * @Package: com.artifex.mupdfdemo
 * @Author: Think
 * @Time: 2016/3/1
 * @desc: TODO
 */
public class PaintView extends View {
    private static final String TAG = "PaintView";
    private Paint  mPaint;
    private Path   mPath;
    private PointF mPointF;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private float  mX, mY;
    private Paint               mBitmapPaint;
    private int                 bitmapWidth;
    private int                 bitmapHeight;
    private DrawPath            dp;
    private ArrayList<DrawPath> savePath;
    private ArrayList<DrawPath> deletePath;
    private static final float INK_THICKNESS = 5.0f;     //墨迹宽度
    private static final int   INK_COLOR     = 0xFFFF0000;
    private              float scale         = 3f;
    protected ArrayList<ArrayList<PointF>> mDrawing;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //得到屏幕的分辨率
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        mPaint = new Paint();
        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - 2 * 45;

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        //画布大小
        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        initCanvas();
        savePath = new ArrayList<>();
        deletePath = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);     //显示旧的画布
        if (mPath != null) {
            // 实时的显示
            canvas.drawPath(mPath, mPaint);
        }
    }

    private void initCanvas() {
        if (mDrawing != null) {
            LogUtils.d(TAG, "paint的绘制");
            mPaint.setAntiAlias(true); //防锯齿
            mPaint.setDither(true); //防抖动

            mPaint.setStrokeJoin(Paint.Join.ROUND);//设置结合处:Miter:锐角， Round:圆弧：BEVEL：直线
            mPaint.setStrokeCap(Paint.Cap.ROUND);

            mPaint.setStyle(Paint.Style.FILL);  //填充
            mPaint.setStrokeWidth(INK_THICKNESS * scale); //控制bold粗细
            mPaint.setColor(Color.parseColor("#ff0000"));

            mBitmapPaint = new Paint(Paint.DITHER_FLAG);


            mCanvas.drawColor(Color.RED);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
/*
            Iterator<ArrayList<PointF>> it = mDrawing.iterator();
            while (it.hasNext()) {
                ArrayList<PointF> arc = it.next();
                if (arc.size() >= 2) {
                    Iterator<PointF> iit = arc.iterator();
                    mPointF = iit.next();
                    float mX = mPointF.x * scale;
                    float mY = mPointF.y * scale;
                    mPath.moveTo(mX, mY);
                    while (iit.hasNext()) {
                        mPointF = iit.next();
                        float x = mPointF.x * scale;
                        float y = mPointF.y * scale;
                        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2); //平滑曲线
                        mX = x;
                        mY = y;
                    }
                    mPath.lineTo(mX, mY);
                } else {
                    mPointF = arc.get(0);
                    mCanvas.drawCircle(mPointF.x * scale, mPointF.y * scale, INK_THICKNESS * scale / 2, mPaint);

                }
            }*/
//            mPaint.setStyle(Paint.Style.STROKE);
        }
    }

    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo(){

        System.out.println(savePath.size()+"--------------");
        if(savePath != null && savePath.size() > 0){
            //调用初始化画布函数以清空画布
            initCanvas();

            //将路径保存列表中的最后一个元素删除 ,并将其保存在路径删除列表中
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);

            //将路径保存列表中的路径重绘在画布上
            Iterator<DrawPath> iter = savePath.iterator();      //重复保存
            while (iter.hasNext()) {
                DrawPath dp = iter.next();
                mCanvas.drawPath(dp.path, dp.paint);

            }
            invalidate();// 刷新
        }
    }
    /**
     * 恢复的核心思想就是将撤销的路径保存到另外一个列表里面(栈)，
     * 然后从redo的列表里面取出最顶端对象，
     * 画在画布上面即可
     */
    public void redo(){
        if(deletePath.size() > 0){
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            //将取出的路径重绘在画布上
            mCanvas.drawPath(dp.path, dp.paint);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }
    /*
     * 清空的主要思想就是初始化画布
     * 将保存路径的两个List清空
     * */
    public void removeAllPaint(){
        //调用初始化画布函数以清空画布
        initCanvas();
        invalidate();//刷新
        savePath.clear();
        deletePath.clear();
    }

    class DrawPath{
        Path path;
        Paint paint;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mPath = new Path();
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;

                mPath.reset();//清空path
                mPath.moveTo(x, y);
                mX = x;
                mY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= 4 || dy >= 4) {
                    mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);//源代码是这样写的，可是我没有弄明白，为什么要这样？
                    mX = x;
                    mY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                savePath.add(dp);
                mPath = null;
                break;
        }
                invalidate();
        return true;
    }
}
