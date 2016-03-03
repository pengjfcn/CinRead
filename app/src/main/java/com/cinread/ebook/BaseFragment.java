package com.cinread.ebook;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Project: mupdfdemo
 * @Package: com.cinread.cindle
 * @Author: Think
 * @Time: 2016/2/26
 * @desc: TODO
 */
public class BaseFragment extends Fragment {
    public View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        return rootView;
    }
}
