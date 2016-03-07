package com.cinread.ebook.pdf;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.artifex.mupdfdemo.R;

import java.util.LinkedList;

/**
 * @Project: CinRead
 * @Package: com.cinread.ebook.pdf
 * @Author: Think
 * @Time: 2016/3/7
 * @desc: TODO
 */
public class SelectPDFAdapter extends BaseAdapter {
    private final LinkedList<SelectPDFItem> mItems;
    private final LayoutInflater            mInflater;

    public SelectPDFAdapter(LayoutInflater inflater) {
        mInflater = inflater;
        mItems = new LinkedList<SelectPDFItem>();
    }

    public void clear() {
        mItems.clear();
    }

    public void add(SelectPDFItem item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    //注释 给不同类型设置不同icon：TODO 不同格式文件不同icon
    private int iconForType(SelectPDFItem.Type type) {
        switch (type) {
            case PARENT:
                return R.drawable.ic_arrow_up;
            case DIR:
                return R.drawable.ic_dir;
            case DOC:
                return R.drawable.ic_doc;
            case IMG:
                return R.drawable.ic_print;
            default:
                return 0;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(R.layout.picker_entry, null);
        } else {
            v = convertView;
        }
        SelectPDFItem item = mItems.get(position);
        ((TextView) v.findViewById(R.id.name)).setText(item.name);
        ((ImageView) v.findViewById(R.id.icon)).setImageResource(iconForType(item.type));
        ((ImageView) v.findViewById(R.id.icon)).setColorFilter(Color.argb(255, 0, 0, 0));
        return v;
    }

}

class ViewHolder {

}