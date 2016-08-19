package com.droidworker.lib.impl;

import com.droidworker.lib.ILoadingLayout;
import com.droidworker.lib.R;
import com.droidworker.lib.constant.Orientation;
import com.droidworker.lib.constant.State;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 实现ILoadingLayout接口,用于header和footer的默认视图
 * @author https://github.com/DroidWorkerLYF
 */
public class LoadingLayout extends FrameLayout implements ILoadingLayout {
    private LinearLayout mContainer;
    private ImageView mImageView;
    private TextView mTextView;
    private Orientation mOrientation;
    private RotateAnimation mRotateAnimation;

    public LoadingLayout(Context context) {
        this(context, Orientation.VERTICAL);
    }

    public LoadingLayout(Context context, Orientation orientation) {
        super(context);
        mOrientation = orientation;

        switch (orientation) {
        case VERTICAL:
        default:
            LayoutInflater.from(context).inflate(R.layout.layout_loading_vertical, this, true);
            break;
        case HORIZONTAL:
            LayoutInflater.from(context).inflate(R.layout.layout_loading_horizontal, this, true);
            break;
        }
        mContainer = (LinearLayout) findViewById(R.id.ll_loading_container);
        mImageView = (ImageView) findViewById(R.id.iv_loading_img);
        mTextView = (TextView) findViewById(R.id.tv_loading_text);

        mRotateAnimation = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setRepeatMode(Animation.RESTART);
    }

    @Override
    public int getSize() {
        switch (mOrientation) {
        case VERTICAL:
        default:
            return mContainer.getHeight();
        case HORIZONTAL:
            return mContainer.getWidth();
        }
    }

    @Override
    public void onPull(State state, float distance) {
        mTextView.setText(String.valueOf(distance));
        if (state == State.UPDATING || state == State.MANUAL_UPDATE) {
            mImageView.startAnimation(mRotateAnimation);
            mTextView.setText(R.string.updating);
        } else if (state == State.LOADING) {
            mImageView.startAnimation(mRotateAnimation);
            mTextView.setText(R.string.loading);
        } else if (state == State.RESET) {
            mImageView.clearAnimation();
        } else {
            if (state == State.PULL_FROM_START) {
                mTextView.setText(R.string.pull_to_update);
            } else if (state == State.PULL_FROM_END) {
                mTextView.setText(R.string.pull_to_load);
            } else if (state == State.RELEASE_TO_UPDATE) {
                mTextView.setText(R.string.release_to_update);
            } else if (state == State.RELEASE_TO_LOAD) {
                mTextView.setText(R.string.release_to_load);
            }
            if (distance != 0) {
                mImageView.setRotation(Math.abs(distance) % getSize() / 100 * 360f);
            }
        }
    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(INVISIBLE);
    }

    @Override
    public View getLoadingView() {
        return this;
    }
}