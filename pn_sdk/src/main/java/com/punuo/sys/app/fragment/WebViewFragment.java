package com.punuo.sys.app.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.punuo.sys.app.R;
import com.punuo.sys.app.util.StatusBarUtil;

/**
 * Created by han.chen.
 * Date on 2019/5/29.
 **/
public abstract class WebViewFragment extends BaseFragment {
    private View mFragmentView;
    private PullToRefreshWebView mPullToRefreshWebView;
    private WebView mWebView;
    private View mStatusBar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.webview_fragment, container, false);
        mPullToRefreshWebView = mFragmentView.findViewById(R.id.pull_to_refresh);
        mWebView = mPullToRefreshWebView.getRefreshableView();
        mStatusBar = mFragmentView.findViewById(R.id.status_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBar.setVisibility(View.VISIBLE);
            mStatusBar.getLayoutParams().height = StatusBarUtil.getStatusBarHeight(getActivity());
            mStatusBar.requestLayout();
        }
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new SampleWebViewClient());
        mPullToRefreshWebView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<WebView>() {

            @Override
            public void onRefresh(PullToRefreshBase<WebView> refreshView) {
                loadUrl();
            }
        });
        loadUrl();
        return mFragmentView;
    }

    public abstract String getUrl();

    public void loadUrl() {
        String url = getUrl();
        if (url != null) {
            mWebView.loadUrl(url);
        }
    }

    private static class SampleWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
