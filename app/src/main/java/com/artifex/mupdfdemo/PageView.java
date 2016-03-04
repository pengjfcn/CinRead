package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cinread.ebook.utils.LogUtils;
import com.cinread.ebook.UIUtils;

import java.util.ArrayList;
import java.util.Iterator;

// Make our ImageViews opaque to optimize redraw
class OpaqueImageView extends ImageView {

    public OpaqueImageView(Context context) {
        super(context);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }
}

interface TextProcessor {
    void onStartLine();

    void onWord(TextWord word);

    void onEndLine();
}

class TextSelector {
    final private TextWord[][] mText;
    final private RectF        mSelectBox;

    public TextSelector(TextWord[][] text, RectF selectBox) {
        mText = text;
        mSelectBox = selectBox;
    }

    public void select(TextProcessor tp) {
        if (mText == null || mSelectBox == null)
            return;

        ArrayList<TextWord[]> lines = new ArrayList<TextWord[]>();
        for (TextWord[] line : mText)
            if (line[0].bottom > mSelectBox.top && line[0].top < mSelectBox.bottom)
                lines.add(line);

        Iterator<TextWord[]> it = lines.iterator();
        while (it.hasNext()) {
            TextWord[] line = it.next();
            boolean firstLine = line[0].top < mSelectBox.top;
            boolean lastLine = line[0].bottom > mSelectBox.bottom;
            float start = Float.NEGATIVE_INFINITY;
            float end = Float.POSITIVE_INFINITY;

            if (firstLine && lastLine) {
                start = Math.min(mSelectBox.left, mSelectBox.right);
                end = Math.max(mSelectBox.left, mSelectBox.right);
            } else if (firstLine) {
                start = mSelectBox.left;
            } else if (lastLine) {
                end = mSelectBox.right;
            }

            tp.onStartLine();

            for (TextWord word : line)
                if (word.right > start && word.left < end)
                    tp.onWord(word);

            tp.onEndLine();
        }
    }
}

//注释  绘制的类
public abstract class PageView extends ViewGroup {
    private static final int    HIGHLIGHT_COLOR       = 0x802572AC;    //高亮颜色  橙灰
    private static final int    LINK_COLOR            = 0x80AC7225;    //链接颜色  橙
    private static final int    BOX_COLOR             = 0xFF4444FF;    //外框颜色  蓝
//    private static final int    INK_COLOR             = 0xFF000000;  //墨迹颜色  黑
    private int    INK_COLOR             = 0xFF000000;

    //    private static final float  INK_THICKNESS         = 5.0f;     //墨迹宽度
    private              float  INK_THICKNESS         = 5.0f;     //墨迹宽度
    private static final int    BACKGROUND_COLOR      = 0xFFFFFFFF;  //背景颜色
    private static final int    PROGRESS_DIALOG_DELAY = 200;  //加载延时时间
    private static final String TAG                   = "PageView";

    //外部设置属性
    public void setINK_THICKNESS(float INK_THICKNESS) {
        this.INK_THICKNESS = INK_THICKNESS;
    }
    public void setINK_COLOR(int INK_COLOR) {
        this.INK_COLOR = INK_COLOR;
    }

    protected final Context mContext;
    protected       int     mPageNumber;
    private         Point   mParentSize;
    protected       Point   mSize;   // Size of page at minimum zoom
    protected       float   mSourceScale;

    private ImageView                           mEntire; // Image rendered at minimum zoom
    private Bitmap                              mEntireBm;
    private Matrix                              mEntireMat;
    private AsyncTask<Void, Void, TextWord[][]> mGetText;
    private AsyncTask<Void, Void, LinkInfo[]>   mGetLinkInfo;
    private CancellableAsyncTask<Void, Void>    mDrawEntire;

    private   Point                            mPatchViewSize; // View size on the basis of which
    // the patch was created
    private   Rect                             mPatchArea;
    private   ImageView                        mPatch;
    private   Bitmap                           mPatchBm;
    private   CancellableAsyncTask<Void, Void> mDrawPatch;
    private   RectF                            mSearchBoxes[];
    protected LinkInfo                         mLinks[];
    private   RectF                            mSelectBox;
    private   TextWord                         mText[][];
    private   RectF                            mItemSelectBox;
    protected ArrayList<ArrayList<PointF>>     mDrawing;
    private   View                             mSearchView;
    private   boolean                          mIsBlank;
    private   boolean                          mHighlightLinks;

    //注释  加载pdf下一页时的 加载中
    private ProgressBar mBusyIndicator;
    private final Handler mHandler = new Handler();

    //注释  创建绘制区域的构造函数
    public PageView(Context c, Point parentSize, Bitmap sharedHqBm) {
        super(c);
        mContext = c;
        mParentSize = parentSize;
        setBackgroundColor(BACKGROUND_COLOR);
        mEntireBm = Bitmap.createBitmap(parentSize.x, parentSize.y, Config.ARGB_8888);
        mPatchBm = sharedHqBm;
        mEntireMat = new Matrix();
    }

    protected abstract CancellableTaskDefinition<Void, Void> getDrawPageTask(Bitmap bm, int sizeX,
                                                                             int sizeY, int
                                                                                     patchX, int
                                                                                     patchY, int
                                                                                     patchWidth,
                                                                             int patchHeight);

    protected abstract CancellableTaskDefinition<Void, Void> getUpdatePageTask(Bitmap bm, int
            sizeX, int sizeY, int patchX, int patchY, int patchWidth, int patchHeight);

    protected abstract LinkInfo[] getLinkInfo();

    protected abstract TextWord[][] getText();

    protected abstract void addMarkup(PointF[] quadPoints, Annotation.Type type);

    private void reinit() {
        // Cancel pending render task
        if (mDrawEntire != null) {
            mDrawEntire.cancelAndWait();
            mDrawEntire = null;
        }

        if (mDrawPatch != null) {
            mDrawPatch.cancelAndWait();
            mDrawPatch = null;
        }

        if (mGetLinkInfo != null) {
            mGetLinkInfo.cancel(true);
            mGetLinkInfo = null;
        }

        if (mGetText != null) {
            mGetText.cancel(true);
            mGetText = null;
        }

        mIsBlank = true;
        mPageNumber = 0;

        if (mSize == null)
            mSize = mParentSize;

        if (mEntire != null) {
            mEntire.setImageBitmap(null);
            mEntire.invalidate();
        }

        if (mPatch != null) {
            mPatch.setImageBitmap(null);
            mPatch.invalidate();
        }

        mPatchViewSize = null;
        mPatchArea = null;

        mSearchBoxes = null;
        mLinks = null;
        mSelectBox = null;
        mText = null;
        mItemSelectBox = null;
    }

    public void releaseResources() {
        reinit();

        if (mBusyIndicator != null) {
            removeView(mBusyIndicator);
            mBusyIndicator = null;
        }
    }

    public void releaseBitmaps() {
        reinit();

        //  recycle bitmaps before releasing them.

        if (mEntireBm != null)
            mEntireBm.recycle();
        mEntireBm = null;

        if (mPatchBm != null)
            mPatchBm.recycle();
        mPatchBm = null;
    }

    //注释 空白区
    public void blank(int page) {
        reinit();
        mPageNumber = page;

        if (mBusyIndicator == null) {
            mBusyIndicator = new ProgressBar(mContext);
            mBusyIndicator.setIndeterminate(true);
            mBusyIndicator.setBackgroundResource(R.drawable.busy);
            addView(mBusyIndicator);
        }

        setBackgroundColor(BACKGROUND_COLOR);
    }

    //注释 设置页面渲染区域
    public void setPage(int page, PointF size) {
        // Cancel pending render task
        if (mDrawEntire != null) {
            mDrawEntire.cancelAndWait();
            mDrawEntire = null;
        }

        mIsBlank = false;
        // Highlights may be missing because mIsBlank was true on last draw
        if (mSearchView != null)
            mSearchView.invalidate();

        mPageNumber = page;
        if (mEntire == null) {
            mEntire = new OpaqueImageView(mContext);
            mEntire.setScaleType(ImageView.ScaleType.MATRIX);
            addView(mEntire);
        }

        // Calculate scaled size that fits within the screen limits
        // This is the size at minimum zoom
        mSourceScale = Math.min(mParentSize.x / size.x, mParentSize.y / size.y);
        Point newSize = new Point((int) (size.x * mSourceScale), (int) (size.y * mSourceScale));
        mSize = newSize;

        mEntire.setImageBitmap(null);
        mEntire.invalidate();

        // Get the link info in the background
        mGetLinkInfo = new AsyncTask<Void, Void, LinkInfo[]>() {
            protected LinkInfo[] doInBackground(Void... v) {
                return getLinkInfo();
            }

            protected void onPostExecute(LinkInfo[] v) {
                mLinks = v;
                if (mSearchView != null)
                    mSearchView.invalidate();
            }
        };

        mGetLinkInfo.execute();

        // Render the page in the background
        mDrawEntire = new CancellableAsyncTask<Void, Void>(getDrawPageTask(mEntireBm, mSize.x,
                mSize.y, 0, 0, mSize.x, mSize.y)) {

            @Override
            public void onPreExecute() {
                setBackgroundColor(BACKGROUND_COLOR);
                mEntire.setImageBitmap(null);
                mEntire.invalidate();

                if (mBusyIndicator == null) {
                    mBusyIndicator = new ProgressBar(mContext);
                    mBusyIndicator.setIndeterminate(true);
                    mBusyIndicator.setBackgroundResource(R.drawable.busy);
                    addView(mBusyIndicator);
                    mBusyIndicator.setVisibility(INVISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (mBusyIndicator != null)
                                mBusyIndicator.setVisibility(VISIBLE);
                        }
                    }, PROGRESS_DIALOG_DELAY);
                }
            }

            @Override
            public void onPostExecute(Void result) {
                removeView(mBusyIndicator);
                mBusyIndicator = null;
                mEntire.setImageBitmap(mEntireBm);
                mEntire.invalidate();
                setBackgroundColor(Color.TRANSPARENT);

            }
        };

        mDrawEntire.execute();

        //注释 新建一个PaintView继承View，能提供一些功能的
        if (mSearchView == null) {
            mSearchView = new View(mContext) {
                @Override
                protected void onDraw(final Canvas canvas) {
                    super.onDraw(canvas);
                    // Work out current total scale factor
                    // from source to view
                    final float scale = mSourceScale * (float) getWidth() / (float) mSize.x;   //注释
                    final Paint paint = new Paint();

                    //当不为空以及搜索页面时，画笔设置高亮颜色，画布绘制选中时的矩形
                    if (!mIsBlank && mSearchBoxes != null) {
                        paint.setColor(HIGHLIGHT_COLOR);
                        for (RectF rect : mSearchBoxes)
                            canvas.drawRect(rect.left * scale, rect.top * scale,
                                    rect.right * scale, rect.bottom * scale,
                                    paint);
                    }

                    //当不为空以及链接界面时，画笔设置高亮颜色，画布绘制选中时的矩形
                    if (!mIsBlank && mLinks != null && mHighlightLinks) {
                        paint.setColor(LINK_COLOR);
                        for (LinkInfo link : mLinks)
                            canvas.drawRect(link.rect.left * scale, link.rect.top * scale,
                                    link.rect.right * scale, link.rect.bottom * scale,
                                    paint);
                    }

                    if (mSelectBox != null && mText != null) {
                        paint.setColor(HIGHLIGHT_COLOR);
                        processSelectedText(new TextProcessor() {
                            RectF rect;

                            public void onStartLine() {
                                rect = new RectF();
                            }

                            public void onWord(TextWord word) {
                                rect.union(word);
                            }

                            public void onEndLine() {
                                if (!rect.isEmpty())
                                    canvas.drawRect(rect.left * scale, rect.top * scale, rect
                                            .right * scale, rect.bottom * scale, paint);
                            }
                        });
                    }

                    if (mItemSelectBox != null) {
                        LogUtils.d(TAG, "mItemSelectBox");
                        Toast.makeText(UIUtils.getContext(), "选中", Toast.LENGTH_SHORT).show();
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(BOX_COLOR);
                        canvas.drawRect(mItemSelectBox.left * scale, mItemSelectBox.top * scale,
                                mItemSelectBox.right * scale, mItemSelectBox.bottom * scale,
                                paint);
                    }

                    //注释  绘制墨迹
                    if (mDrawing != null) {
                        LogUtils.d(TAG, "paint的绘制");
                        Path path = new Path();

                        PointF p;

                        //增加撤销和恢复笔迹的功能

                        //						if (this.isHardwareAccelerated()){
                        //							LogUtils.d(TAG,"已经启动硬件加速");
                        //						}

                        paint.setAntiAlias(true); //防锯齿
                        paint.setDither(true); //防抖动

                        paint.setStrokeJoin(Paint.Join.ROUND);//设置结合处:Miter:锐角， Round:圆弧：BEVEL：直线
                        paint.setStrokeCap(Paint.Cap.ROUND);

                        paint.setStyle(Paint.Style.FILL);  //填充
                        paint.setStrokeWidth(INK_THICKNESS * scale); //控制bold粗细
                        paint.setColor(INK_COLOR);

                        Iterator<ArrayList<PointF>> it = mDrawing.iterator();
                        while (it.hasNext()) {
                            ArrayList<PointF> arc = it.next();
                            LogUtils.d(TAG, "out");
                            if (arc.size() >= 2) {
                                Iterator<PointF> iit = arc.iterator();
                                p = iit.next();
                                float mX = p.x * scale;
                                float mY = p.y * scale;
                                path.moveTo(mX, mY);
                                while (iit.hasNext()) {
                                    LogUtils.d(TAG, mX + " -in- " + mY);
                                    p = iit.next();
                                    float x = p.x * scale;
                                    float y = p.y * scale;
                                    path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2); //平滑曲线
                                    mX = x;
                                    mY = y;
                                }
                                path.lineTo(mX, mY);
                            } else {
                                p = arc.get(0);
                                canvas.drawCircle(p.x * scale, p.y * scale, INK_THICKNESS * scale
                                        / 2, paint);
                            }
                        }

                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawPath(path, paint);
                    }
                }
            };

            addView(mSearchView);
        }
        //重新布局了一下view
        requestLayout();
    }

    public void setSearchBoxes(RectF searchBoxes[]) {
        mSearchBoxes = searchBoxes;
        if (mSearchView != null)
            mSearchView.invalidate();
    }

    public void setLinkHighlighting(boolean f) {
        mHighlightLinks = f;
        if (mSearchView != null)
            mSearchView.invalidate();
    }

    public void deselectText() {
        mSelectBox = null;
        mSearchView.invalidate();
    }

    public void selectText(float x0, float y0, float x1, float y1) {
        float scale = mSourceScale * (float) getWidth() / (float) mSize.x;
        float docRelX0 = (x0 - getLeft()) / scale;
        float docRelY0 = (y0 - getTop()) / scale;
        float docRelX1 = (x1 - getLeft()) / scale;
        float docRelY1 = (y1 - getTop()) / scale;
        // Order on Y but maintain the point grouping
        if (docRelY0 <= docRelY1)
            mSelectBox = new RectF(docRelX0, docRelY0, docRelX1, docRelY1);
        else
            mSelectBox = new RectF(docRelX1, docRelY1, docRelX0, docRelY0);

        mSearchView.invalidate();

        if (mGetText == null) {
            mGetText = new AsyncTask<Void, Void, TextWord[][]>() {
                @Override
                protected TextWord[][] doInBackground(Void... params) {
                    return getText();
                }

                @Override
                protected void onPostExecute(TextWord[][] result) {
                    mText = result;
                    mSearchView.invalidate();
                }
            };

            mGetText.execute();
        }
    }

    public void startDraw(float x, float y) {
        float scale = mSourceScale * (float) getWidth() / (float) mSize.x;
        float docRelX = (x - getLeft()) / scale;
        float docRelY = (y - getTop()) / scale;
        if (mDrawing == null)
            mDrawing = new ArrayList<ArrayList<PointF>>();

        ArrayList<PointF> arc = new ArrayList<PointF>();
        arc.add(new PointF(docRelX, docRelY));
        mDrawing.add(arc);
        mSearchView.invalidate();
    }

    public void continueDraw(float x, float y) {
        float scale = mSourceScale * (float) getWidth() / (float) mSize.x;
        float docRelX = (x - getLeft()) / scale;
        float docRelY = (y - getTop()) / scale;

        if (mDrawing != null && mDrawing.size() > 0) {
            ArrayList<PointF> arc = mDrawing.get(mDrawing.size() - 1);
            arc.add(new PointF(docRelX, docRelY));
            mSearchView.invalidate();
        }
    }

    //取消 绘制
    public void cancelDraw() {
        //置空当前绘制的内容
        mDrawing = null;
        mSearchView.invalidate();
    }

    //撤销上一步
    public void undoDraw() {
        //置空当前绘制的内容
        if (mDrawing!=null&&mDrawing.size() > 0) {
            mDeleteDrawing.add(mDrawing.remove(mDrawing.size() - 1));
            mSearchView.invalidate();
        }
    }

    //注释  将mDrawing对象存入集合中，再通过接口的方式提供出去，
    private ArrayList<ArrayList<PointF>> mDeleteDrawing = new ArrayList<ArrayList<PointF>>();

    //恢复下一步
    public void redoDraw() {
        if (mDeleteDrawing!=null&&mDeleteDrawing.size()>0) {
            mDrawing.add(mDeleteDrawing.remove(mDeleteDrawing.size() - 1));
            mSearchView.invalidate();
        }
    }

    protected PointF[][] getDraw() {
        if (mDrawing == null)
            return null;

        PointF[][] path = new PointF[mDrawing.size()][];

        for (int i = 0; i < mDrawing.size(); i++) {
            ArrayList<PointF> arc = mDrawing.get(i);
            path[i] = arc.toArray(new PointF[arc.size()]);
        }

        return path;
    }

    protected void processSelectedText(TextProcessor tp) {
        (new TextSelector(mText, mSelectBox)).select(tp);
    }

    //注释 如果选中了编辑方形区域就触发view成选中状态
    public void setItemSelectBox(RectF rect) {
        mItemSelectBox = rect;
        if (mSearchView != null)
            mSearchView.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int x, y;
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                x = mSize.x;
                break;
            default:
                x = MeasureSpec.getSize(widthMeasureSpec);
        }
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                y = mSize.y;
                break;
            default:
                y = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(x, y);

        if (mBusyIndicator != null) {
            int limit = Math.min(mParentSize.x, mParentSize.y) / 2;
            mBusyIndicator.measure(MeasureSpec.AT_MOST | limit, MeasureSpec.AT_MOST | limit);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int w = right - left;
        int h = bottom - top;

        if (mEntire != null) {
            if (mEntire.getWidth() != w || mEntire.getHeight() != h) {
                mEntireMat.setScale(w / (float) mSize.x, h / (float) mSize.y);
                mEntire.setImageMatrix(mEntireMat);
                mEntire.invalidate();
            }
            mEntire.layout(0, 0, w, h);
        }

        if (mSearchView != null) {
            mSearchView.layout(0, 0, w, h);
        }

        if (mPatchViewSize != null) {
            if (mPatchViewSize.x != w || mPatchViewSize.y != h) {
                // Zoomed since patch was created
                mPatchViewSize = null;
                mPatchArea = null;
                if (mPatch != null) {
                    mPatch.setImageBitmap(null);
                    mPatch.invalidate();
                }
            } else {
                mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea
                        .bottom);
            }
        }

        if (mBusyIndicator != null) {
            int bw = mBusyIndicator.getMeasuredWidth();
            int bh = mBusyIndicator.getMeasuredHeight();

            mBusyIndicator.layout((w - bw) / 2, (h - bh) / 2, (w + bw) / 2, (h + bh) / 2);
        }
    }

    public void updateHq(boolean update) {
        Rect viewArea = new Rect(getLeft(), getTop(), getRight(), getBottom());
        //注释  矩形绘制区域


        if (viewArea.width() == mSize.x || viewArea.height() == mSize.y) {
            // If the viewArea's size matches the unzoomed size, there is no need for an hq patch
            if (mPatch != null) {
                mPatch.setImageBitmap(null);
                mPatch.invalidate();
            }
        } else {
            final Point patchViewSize = new Point(viewArea.width(), viewArea.height());
            final Rect patchArea = new Rect(0, 0, mParentSize.x, mParentSize.y);

            // Intersect and test that there is an intersection
            if (!patchArea.intersect(viewArea))
                return;

            // Offset patch area to be relative to the view top left
            patchArea.offset(-viewArea.left, -viewArea.top);

            boolean area_unchanged = patchArea.equals(mPatchArea) && patchViewSize.equals
                    (mPatchViewSize);

            // If being asked for the same area as last time and not because of an update then
            // nothing to do
            if (area_unchanged && !update)
                return;

            boolean completeRedraw = !(area_unchanged && update);

            // Stop the drawing of previous patch if still going
            if (mDrawPatch != null) {
                mDrawPatch.cancelAndWait();
                mDrawPatch = null;
            }

            // Create and add the image view if not already done
            if (mPatch == null) {
                mPatch = new OpaqueImageView(mContext);
                mPatch.setScaleType(ImageView.ScaleType.MATRIX);
                addView(mPatch);
                mSearchView.bringToFront();
            }

            CancellableTaskDefinition<Void, Void> task;

            if (completeRedraw)
                task = getDrawPageTask(mPatchBm, patchViewSize.x, patchViewSize.y,
                        patchArea.left, patchArea.top,
                        patchArea.width(), patchArea.height());
            else
                task = getUpdatePageTask(mPatchBm, patchViewSize.x, patchViewSize.y,
                        patchArea.left, patchArea.top,
                        patchArea.width(), patchArea.height());

            mDrawPatch = new CancellableAsyncTask<Void, Void>(task) {

                public void onPostExecute(Void result) {
                    mPatchViewSize = patchViewSize;
                    mPatchArea = patchArea;
                    mPatch.setImageBitmap(mPatchBm);
                    mPatch.invalidate();
                    //requestLayout();
                    // Calling requestLayout here doesn't lead to a later call to layout. No idea
                    // why, but apparently others have run into the problem.
                    mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea
                            .bottom);
                }
            };

            mDrawPatch.execute();
        }
    }

    public void update() {
        // Cancel pending render task
        if (mDrawEntire != null) {
            mDrawEntire.cancelAndWait();
            mDrawEntire = null;
        }

        if (mDrawPatch != null) {
            mDrawPatch.cancelAndWait();
            mDrawPatch = null;
        }


        // Render the page in the background
        mDrawEntire = new CancellableAsyncTask<Void, Void>(getUpdatePageTask(mEntireBm, mSize.x,
                mSize.y, 0, 0, mSize.x, mSize.y)) {

            public void onPostExecute(Void result) {
                mEntire.setImageBitmap(mEntireBm);
                mEntire.invalidate();
            }
        };

        mDrawEntire.execute();

        updateHq(true);
    }

    public void removeHq() {
        // Stop the drawing of the patch if still going
        if (mDrawPatch != null) {
            mDrawPatch.cancelAndWait();
            mDrawPatch = null;
        }

        // And get rid of it
        mPatchViewSize = null;
        mPatchArea = null;
        if (mPatch != null) {
            mPatch.setImageBitmap(null);
            mPatch.invalidate();
        }
    }

    public int getPage() {
        return mPageNumber;
    }

    @Override
    public boolean isOpaque() {
        return true;
    }
}
