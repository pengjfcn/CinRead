package com.cinread.ebook;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
public class ItemAdapter extends BaseAdapter {
    private List<ResolveInfo> mList;//定义一个list对象
    public static final int APP_PAGE_SIZE = 8;//每一页装载数据的大小
    private PackageManager pm;//定义一个PackageManager对象
    private Context        mContext;

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param list    所有APP的集合
     * @param page    当前页
     */
    public ItemAdapter(Context context, List<ResolveInfo> list, int page) {
        mContext = context;
        pm = context.getPackageManager();
        mList = new ArrayList<ResolveInfo>();
        //根据当前页计算装载的应用，每页只装载8个
        int i = page * APP_PAGE_SIZE;//当前页的起始位置
        int iEnd = i + APP_PAGE_SIZE;//所有数据的结束位置
        while ((i < list.size()) && (i < iEnd)) {
            mList.add(list.get(i));
            i++;
        }
    }

    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    public Object getItem(int position) {
        if (mList != null) {
            return mList.get(position);
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(UIUtils.getContext()).inflate(R.layout.app_item, parent, false);
            //convertView = View.inflate(UIUtils.getContext(),R.layout.app_item,null);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivAppIcon);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvAppName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ResolveInfo appInfo = mList.get(position);
        holder.ivIcon.setImageDrawable(appInfo.loadIcon(pm));
        holder.tvName.setText(appInfo.loadLabel(pm));
        return convertView;
    }

    private class ViewHolder {
        ImageView ivIcon;
        TextView  tvName;
    }

}
