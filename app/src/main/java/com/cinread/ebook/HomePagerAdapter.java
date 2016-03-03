package com.cinread.ebook;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.List;

/**
 * @Project: mupdfdemo
 * @Package: com.cinread.cindle
 * @Author: Think
 * @Time: 2016/2/26
 * @desc: TODO
 */

public class HomePagerAdapter extends PagerAdapter {
    private List<GridView> array;

    public HomePagerAdapter(Context context, List<GridView> array) {
        this.array = array;
    }

    @Override
    public int getCount() {
        if (array != null) {
            return array.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView(array.get(position));
        return array.get(position);
    }

    @Override
    public void destroyItem(View container, int position, Object obj) {
        ((ViewPager) container).removeView((View) obj);
    }
}

