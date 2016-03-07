package com.cinread.ebook;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.artifex.mupdfdemo.ChoosePDFActivity;
import com.artifex.mupdfdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Project: mupdfdemo
 * @Package: com.cinread.cindle
 * @Author: Think
 * @Time: 2016/2/26
 * @desc: TODO
 */
public class HomeActivity extends Activity implements AdapterView.OnItemClickListener {
    public static final  String TAG           = "HomeActivity";
    private static final float  APP_PAGE_SIZE = 8.0f;

    private FrameLayout  mFrameLayout;  //TODO 上方无内容
    private ViewPager    mViewPager;
    private LinearLayout mDotContainer;

    private HomePagerAdapter  mAdapter;
    private ArrayList<GridView> mGridViews;

    //    private List<ImageView> mDatas;  //图标
    //    private int[] ICONS = {R.drawable.ic_doc, R.drawable.ic_dir};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.homepage);
        init();
        initView();
        initAdapter();
        initListener();
    }

    private void init() {
        mFrameLayout = (FrameLayout) findViewById(R.id.homepage_fl_content);
        mViewPager = (ViewPager) findViewById(R.id.homepage_vp_bottom);
        mDotContainer = (LinearLayout) findViewById(R.id.dots_container);
    }

    private void initView() {
        //上方
//        ListView listView = View.inflate(UIUtils.getContext(),R.layout.,null);
//        mFrameLayout.addView();

        //下方
        //得到包管理器
        final PackageManager packageManager = getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // get all apps
        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        // the total pages
        final int PageCount = (int) Math.ceil(apps.size() / APP_PAGE_SIZE);
        mGridViews = new ArrayList<GridView>();
        for (int i = 0; i < PageCount; i++) {
            //设置相关属性
            GridView gridView = new GridView(this);
            gridView.setCacheColorHint(Color.TRANSPARENT);
            gridView.setSelector(new ColorDrawable());
            gridView.setHorizontalSpacing(2);
            gridView.setVerticalSpacing(2);
            gridView.setNumColumns(4);

            //设置适配器
            gridView.setAdapter(new ItemAdapter(this, apps, i));
            //设置点击事件
            gridView.setOnItemClickListener(this);
            //添加到集合
            mGridViews.add(gridView);

            //添加动态点
            addDot(i);
        }
    }

    private void addDot(int i) {
        //动态点
        View dot = new View(this);
        dot.setBackgroundResource(R.drawable.dot_normal);
        //布局参数，使用父容器的布局参数
        int tt = DisplayUtils.dip2px(this, 20f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(tt, tt);
        if (i != 0) {
            layoutParams.leftMargin = tt;
            layoutParams.bottomMargin = tt;
        } else {
            dot.setBackgroundResource(R.drawable.dot_selected);
        }

        mDotContainer.addView(dot, layoutParams);
    }

    private void initAdapter() {
        mAdapter = new HomePagerAdapter(UIUtils.getContext(), mGridViews);
        mViewPager.setAdapter(mAdapter);
    }

    //设置viewpager的滑动监听
    private void initListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int
                    positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int childCount = mDotContainer.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = mDotContainer.getChildAt(i);
                    childAt.setBackgroundResource(position == i ? R.drawable.dot_selected : R
                            .drawable.dot_normal);

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(HomeActivity.this, "Yahoo! cindle：" + position, Toast.LENGTH_SHORT).show();
        //模拟假数据 0 和 1 进入 PDF界面
        if (id == 0 || id == 1){
            Intent intent = new Intent(this, ChoosePDFActivity.class);
            startActivity(intent);
        }
    }
}
