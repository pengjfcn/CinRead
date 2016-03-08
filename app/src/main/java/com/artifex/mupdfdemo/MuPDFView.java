package com.artifex.mupdfdemo;

import android.graphics.PointF;
import android.graphics.RectF;

enum Hit {Nothing, Widget, Annotation};

/**
 * 适配器模式 接口
 * 注释：方便统一管理，增加方法（拓展功能），添加功能就是添加方法
 *
 * public class Source {
 2.
 3.    public void method1() {
 4.        System.out.println("this is original method!");
 5.    }
 6.}
 *
 * public interface Targetable {
 2.
 3.    /* 与原类中的方法相同
4.    public void method1();
		5.
		6.    /* 新类的方法
		7.    public void method2();
		8.}
 *
 * public class Adapter extends Source implements Targetable {
 2.
 3.    @Override
 4.    public void method2() {
 5.        System.out.println("this is the targetable method!");
 6.    }
 7.}
 *
 */

public interface MuPDFView {
	public void setPage(int page, PointF size);
	public void setScale(float scale);
	public int getPage();
	public void blank(int page);
	public Hit passClickEvent(float x, float y);
	public LinkInfo hitLink(float x, float y);
	public void selectText(float x0, float y0, float x1, float y1);
	public void deselectText();
	public boolean copySelection();
	public boolean markupSelection(Annotation.Type type);
	public void deleteSelectedAnnotation();
	public void setSearchBoxes(RectF searchBoxes[]);
	public void setLinkHighlighting(boolean f);
	public void deselectAnnotation();
	public void startDraw(float x, float y);
	public void continueDraw(float x, float y);
	public void cancelDraw();
//	public void cancelDraw(int i); //撤销
	public void undoDraw(); //撤销
	public void redoDraw(); //恢复
	public void clearDraw(); //清空
	public boolean saveDraw();
	public void setChangeReporter(Runnable reporter);
	public void update();
	public void updateHq(boolean update);
	public void removeHq();
	public void releaseResources();
	public void releaseBitmaps();

	//注释  添加设置color和bold
	public void setINK_THICKNESS(float INK_THICKNESS);
	public void setINK_COLOR(int INK_COLOR);
}
