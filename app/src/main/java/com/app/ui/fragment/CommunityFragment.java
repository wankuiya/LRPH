package com.app.ui.fragment;


import android.support.v4.app.Fragment;

import com.punuo.sys.app.fragment.WebViewFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommunityFragment extends WebViewFragment {

    @Override
    public String getUrl() {
        return "http://118.31.71.150:8888/mobilecommunity/";
    }
}

