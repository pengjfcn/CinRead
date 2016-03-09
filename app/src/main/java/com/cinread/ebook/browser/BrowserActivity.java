package com.cinread.ebook.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.artifex.mupdfdemo.R;

import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Project: CinRead
 * @Package: com.cinread.ebook
 * @Author: Think
 * @Time: 2016/3/8
 * @desc: TODO
 */
public abstract class BrowserActivity extends Activity {
    private BrowserAdapter adapter;
    private static final String                          CURRENT_DIRECTORY   = "currentDirectory";
    private final        AdapterView.OnItemClickListener onItemClickListener = new AdapterView
            .OnItemClickListener() {
        @SuppressWarnings({"unchecked"})
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final File file = ((AdapterView<BrowserAdapter>) adapterView).getAdapter().getItem(i);
            if (file.isDirectory()) {
                setCurrentDir(file);
            } else {
                showDocument(file);
            }
        }
    };
    private         UriBrowserAdapter recentAdapter;
    private         ViewerPreferences viewerPreferences;
    protected final FileFilter        filter;
    private SimpleAdapter mListAdapter;

    public BrowserActivity() {
        this.filter = createFileFilter();
    }

    protected abstract FileFilter createFileFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        viewerPreferences = new ViewerPreferences(this);
        final ListView browseList = initBrowserListView();
        final ListView recentListView = initRecentListView();
        TabHost tabHost = (TabHost) findViewById(R.id.browserTabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("Browse").setIndicator("Browse").setContent(new TabHost
                .TabContentFactory() {
            public View createTabContent(String s) {
                return browseList;
            }
        }));
        tabHost.addTab(tabHost.newTabSpec("Recent").setIndicator("Recent").setContent(new TabHost
                .TabContentFactory() {
            public View createTabContent(String s) {
                return recentListView;
            }
        }));
        tabHost.addTab(tabHost.newTabSpec("Recent").setIndicator("Recent").setContent(new TabHost
                .TabContentFactory() {
            public View createTabContent(String s) {
                return recentListView;
            }
        }));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final File sdcardPath = new File("/sdcard");
        if (sdcardPath.exists()) {
            setCurrentDir(sdcardPath);
        } else {
            setCurrentDir(new File("/"));
        }
        if (savedInstanceState != null) {
            final String absolutePath = savedInstanceState.getString(CURRENT_DIRECTORY);
            if (absolutePath != null) {
                setCurrentDir(new File(absolutePath));
            }
        }
    }

    private ListView initBrowserListView() {
        final ListView listView = new ListView(this);
        adapter = new BrowserAdapter(this, filter);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private List<Map<String, Object>> fileList = new ArrayList<>();
    private List<Bookmark> data; //注释：跟txt文本相关的data

    private ListView initRecentListView() {
        ListView listView = new ListView(this);
        //注释 修改
        TextView tv = new TextView(this);
        tv.setText("empty activity");
        listView.setEmptyView(tv);

        mListAdapter = new SimpleAdapter(this, fileList, R.layout.list_item_bookmark,
                new String[]{"image", "filename", "per"}, new int[]{R.id.list_image, R.id
                .list_text, R.id.per});
        listView.setAdapter(mListAdapter);

        /*################### 注释 #################*/

        recentAdapter = new UriBrowserAdapter();
//        listView.setAdapter(recentAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressWarnings({"unchecked"})
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDocument(((AdapterView<UriBrowserAdapter>) adapterView).getAdapter().getItem
                        (i));
            }
        });
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private void showDocument(File file) {
        showDocument(Uri.fromFile(file));
    }

    protected abstract void showDocument(Uri uri);

    private void setCurrentDir(File newDir) {
        adapter.setCurrentDirectory(newDir);
        getWindow().setTitle(newDir.getAbsolutePath());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_DIRECTORY, adapter.getCurrentDirectory().getAbsolutePath());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        recentAdapter.setUris(viewerPreferences.getRecent());
    }

    //注释

    //打开txt文本格式的逻辑
    private void open(Bookmark bookmark) {
        Intent intent = new Intent(BrowserActivity.this, MuPDFActivity.class);
        intent.setData(Uri.parse("file://" + bookmark.getPath()));
        startActivity(intent);
    }

    private void getBookmarks() {
        LitePalApplication.initialize(this);
        data = DataSupport.order("modifyDate DESC").find(Bookmark.class);
    }

    private void refresh() {
        getBookmarks();
        if (null != data && !data.isEmpty()) {
            fileList.clear();
            Map<String, Object> bookmark;
            DecimalFormat sirPercent = new DecimalFormat("#0.00");
            String name;
            for (Bookmark item : data) {
                bookmark = new HashMap<String, Object>();
                bookmark.put("image", R.drawable.icon);
                bookmark.put("filename", item.getShowName());
                bookmark.put("per", sirPercent.format(item.getPercent()) + "%");
                fileList.add(bookmark);
            }

            mListAdapter.notifyDataSetChanged();
        }
    }
}
