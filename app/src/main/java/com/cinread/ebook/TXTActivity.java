package com.cinread.ebook;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import com.cinread.ebook.ReadView.ReadView;

import java.io.File;

/**
 * @Project: CinRead
 * @Package: com.cinread.ebook
 * @Author: Think
 * @Time: 2016/3/4
 * @desc: TODO
 */
public class TXTActivity extends Activity {
    private ReadView readView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File dir = null;
        Uri fileUri = getIntent().getData();
        if (fileUri != null) {
            dir = new File(fileUri.getPath());
        }
        readView = null;
        if (dir != null) {
            readView = new ReadView(this,dir.getPath());
        }
        else
            finish();
        setContentView(readView);
    }
}
