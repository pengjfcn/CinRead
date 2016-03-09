package com.cinread.ebook.browser;

import android.content.Intent;
import android.net.Uri;

import com.artifex.mupdfdemo.MuPDFActivity;

import java.io.FileFilter;

public class MainBrowserActivity extends BrowserActivity
{

    @Override
    protected FileFilter createFileFilter()
    {
        return null;
    }

    @Override
    protected void showDocument(Uri uri)
    {
        /*final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        String uriString = uri.toString();
        String extension = uriString.substring(uriString.lastIndexOf('.') + 1);
        startActivity(intent);*/
        Intent intent = new Intent(this, MuPDFActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }
}
