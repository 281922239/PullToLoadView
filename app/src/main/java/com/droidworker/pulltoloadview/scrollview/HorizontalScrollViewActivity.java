package com.droidworker.pulltoloadview.scrollview;

import com.droidworker.lib.PullToLoadBaseView;
import com.droidworker.lib.impl.scrollview.PullToLoadHorizontalScrollView;
import com.droidworker.pulltoloadview.BaseActivity;
import com.droidworker.pulltoloadview.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

/**
 * @author luoyanfeng@le.com
 */
public class HorizontalScrollViewActivity extends BaseActivity {
    private PullToLoadHorizontalScrollView mPullToLoadHorizontalScrollView;

    @Override
    public void onLoadNew() {
        mPullToLoadHorizontalScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToLoadHorizontalScrollView.onLoadComplete();
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        mPullToLoadHorizontalScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToLoadHorizontalScrollView.onLoadComplete();
            }
        }, 2000);
    }

    @Override
    protected PullToLoadBaseView getPullToLoadView() {
        return mPullToLoadHorizontalScrollView;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontalscrollview);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        setTitle(R.string.demo_6_title);

        mPullToLoadHorizontalScrollView = (PullToLoadHorizontalScrollView) findViewById(R.id.horizontalscrollview);
        if(mPullToLoadHorizontalScrollView != null){
            mPullToLoadHorizontalScrollView.setOnPullToLoadListener(this);
        }
    }
}