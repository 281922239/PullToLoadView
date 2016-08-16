package com.droidworker.lib;

import com.droidworker.lib.constant.Direction;
import com.droidworker.lib.constant.LoadMode;
import com.droidworker.lib.constant.Orientation;
import com.droidworker.lib.constant.State;
import com.droidworker.lib.impl.LoadingLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 所有支持刷新和加载更多视图的父类,定义了touch事件的处理,和基本的方法.
 * @author https://github.com/DroidWorkerLYF
 */
public abstract class PullToLoadBaseView<T extends ViewGroup> extends FrameLayout
        implements IPullToLoad<T> {
    private static final String TAG = "PullToLoadBaseView";
    private static final float FRICTION = 2.0f;
    private static final int[] THEME_ATTRS = { android.R.attr.actionBarSize };
    /**
     * Header view
     */
    private LoadingLayout mHeader;
    /**
     * Footer view
     */
    private LoadingLayout mFooter;
    private LoadingLayout mCurUpdateLayout;
    /**
     * 实际显示内容的view
     */
    private T mContentView;
    /**
     * 是否在Z轴上位于Toolbar或者自定义的导航栏下方.
     */
    private boolean mIsUnderBar;
    /**
     * 指定的内容view id
     */
    private int mContentViewId;
    /**
     * 支持的加载模式
     */
    private LoadMode mLoadMode = LoadMode.PULL_FROM_START;
    /**
     * 当前出于的加载模式
     */
    private LoadMode mCurLoadMode;
    /**
     * 当前出于的状态
     */
    private State mState = State.RESET;
    /**
     * ActionBar高度,默认取系统的android.R.attr.actionBarSize
     */
    private int mActionBarSize;
    /**
     * 是否intercept touch事件
     */
    private boolean mIsIntercepted;
    private int mTouchSlop;
    /**
     * 上一次触摸事件的坐标
     */
    private float mEndX, mEndY;
    /**
     * 触摸事件起始的坐标
     */
    private float mStartX, mStartY;
    /**
     * 加载状态回调
     */
    private PullToLoadListener mPullToLoadListener;

    public PullToLoadBaseView(Context context) {
        this(context, null);
    }

    public PullToLoadBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PullToLoadView);
        mIsUnderBar = typedArray.getBoolean(R.styleable.PullToLoadView_underBar, false);
        mContentViewId = typedArray.getResourceId(R.styleable.PullToLoadView_content_view_id, 0);
        typedArray.recycle();

        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();

        mActionBarSize = getActionBarSize();

        initView();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            updateUI(mIsUnderBar);
        }
    }

    private void initView() {
        mContentView = createContentView(mContentViewId);
        addContentView(mContentView);

        mHeader = createHeader();
        addHeader(mHeader);

        mFooter = createFooter();
        addViewInternal(mFooter, getLoadingLayoutLayoutParams());

        bringChildToFront(mContentView);
    }

    /**
     * 获取滚动方向, {@link Orientation#VERTICAL}或者{@link Orientation#HORIZONTAL},
     * 所有涉及方向的地方,都是用垂直方向作为default
     * @return 滚动方向
     */
    protected abstract Orientation getScrollOrientation();

    /**
     * 创建header,必须继承自{@link LoadingLayout}
     * @return header
     */
    protected abstract LoadingLayout createHeader();

    /**
     * 创建footer,必须继承自{@link LoadingLayout}
     * @return footer
     */
    protected abstract LoadingLayout createFooter();

    /**
     * 创建内容区域视图
     * @param layoutId 指定用来inflate视图的id
     * @return content view
     */
    protected abstract T createContentView(int layoutId);

    /**
     * 将contentView添加到父容器中
     * @param contentView 内容视图
     */
    private void addContentView(T contentView) {
        addViewInternal(contentView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * 因为Override了addView方法,所以提供此方法,用来在父容器中添加视图
     * @param child 要添加的视图
     * @param index 添加到的位置,-1表示添加到最后一项
     * @param params 布局参数
     */
    protected final void addViewInternal(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    /**
     * {@link #addViewInternal(View, int, ViewGroup.LayoutParams)}
     */
    protected final void addViewInternal(View child, ViewGroup.LayoutParams params) {
        this.addViewInternal(child, -1, params);
    }

    /**
     * 将header添加到父布局中
     * @param loadingLayout header
     */
    protected void addHeader(LoadingLayout loadingLayout) {
        FrameLayout.LayoutParams layoutParams = getLoadingLayoutLayoutParams();
        switch (getScrollOrientation()) {
        case VERTICAL:
        default:
            layoutParams.gravity = Gravity.TOP;
        case HORIZONTAL:
            layoutParams.gravity = Gravity.START;
        }
        addViewInternal(loadingLayout, 0, layoutParams);
    }

    /**
     * 获取用于header和footer的布局参数
     * @return 布局参数
     */
    private FrameLayout.LayoutParams getLoadingLayoutLayoutParams() {
        switch (getScrollOrientation()) {
        case VERTICAL:
        default:
            return new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
        case HORIZONTAL:
            return new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT);
        }
    }

    /**
     * 更新UI
     * @param isUnderBar {@link #mIsUnderBar}
     */
    private void updateUI(boolean isUnderBar) {
        if (isUnderBar) {
             mContentView.setClipToPadding(false);
        } else {
            mContentView.setClipToPadding(true);
        }
        final int headerSize = mHeader.getSize();
        switch (getScrollOrientation()) {
        case VERTICAL:
        default: {
            mContentView.setPadding(0, headerSize, 0, 0);
        }
            break;
        case HORIZONTAL: {
            mContentView.setPadding(headerSize, 0, 0, 0);
        }
            break;
        }
        updateContentUI(isUnderBar);
    }

    /**
     * @return ActionBar高度
     */
    private int getActionBarSize() {
        final TypedArray a = getContext().obtainStyledAttributes(THEME_ATTRS);
        try {
            return a.getDimensionPixelSize(0,
                    getResources().getDimensionPixelSize(R.dimen.actionBarSize));
        } finally {
            a.recycle();
        }
    }

    protected abstract void updateContentUI(boolean isUnderBar);

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        // 将child添加到内容视图中
        mContentView.addView(child, index, params);
    }

    @Override
    public void setMode(LoadMode loadMode) {
        mLoadMode = loadMode;
    }

    @Override
    public LoadMode getMode() {
        return mLoadMode;
    }

    @Override
    public T getContentView() {
        return mContentView;
    }

    @Override
    public boolean isLoading() {
        return mState == State.LOADING || mState == State.MANUAL_LOAD;
    }

    @Override
    public void setLoading() {
        mState = State.MANUAL_LOAD;
    }

    @Override
    public void onLoadComplete() {
        if (isLoading()) {
            setState(State.RESET);
        }
    }

    @Override
    public void setOnPullToLoadListener(PullToLoadListener pullToLoadListener) {
        mPullToLoadListener = pullToLoadListener;
    }

    @Override
    public void onPull(State state, float distance) {
        if (mCurUpdateLayout == null) {
            return;
        }
        mCurUpdateLayout.onPull(state, distance);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mLoadMode.isPullToLoad()) {
            Log.i(TAG, "intercept pull to load not enable");
            return false;
        }

        final int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            Log.i(TAG, "intercept action cancel || up");
            mIsIntercepted = false;
            return false;
        }
        if (action != MotionEvent.ACTION_DOWN && mIsIntercepted) {
            Log.i(TAG, "intercept action is not down and is intercepted");
            return true;
        }

        switch (action) {
        case MotionEvent.ACTION_DOWN: {
            if (isReadyToPull()) {
                Log.i(TAG, "intercept action down");
                mEndX = mStartX = event.getX();
                mEndY = mStartY = event.getY();
                mIsIntercepted = false;
            }
        }
            break;
        case MotionEvent.ACTION_MOVE: {
            if (!isReadyToPull()) {
                return false;
            }
            if (isLoading()) {
                Log.i(TAG, "intercept action move is loading");
                return true;
            }
            final float x = event.getX(), y = event.getY();
            final float scrollDirectionMove;
            final float otherDirectionMove;
            switch (getScrollOrientation()) {
            case VERTICAL:
            default:
                scrollDirectionMove = y - mEndY;
                otherDirectionMove = x - mEndX;
                break;
            case HORIZONTAL:
                scrollDirectionMove = x - mEndX;
                otherDirectionMove = y - mEndY;
                break;

            }
            final float absMove = Math.abs(scrollDirectionMove);
            if (absMove > mTouchSlop && absMove > Math.abs(otherDirectionMove)) {
                Log.e(TAG, scrollDirectionMove + "");
                if (scrollDirectionMove >= 1f && isReadyToPullStart()) {
                    mEndX = x;
                    mEndY = y;
                    mIsIntercepted = true;
                    mCurLoadMode = LoadMode.PULL_FROM_START;
                    setState(State.PULL_FROM_START);
                    Log.i(TAG, "intercept action move pull from start");
                } else if (scrollDirectionMove <= -1f && isReadyToPullEnd()) {
                    mEndX = x;
                    mEndY = y;
                    mIsIntercepted = true;
                    mCurLoadMode = LoadMode.PULL_FROM_END;
                    setState(State.PULL_FROM_END);
                    Log.i(TAG, "intercept action move pull from end");
                }
            }
        }
            break;
        }

        return mIsIntercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mLoadMode.isPullToLoad()) {
            Log.i(TAG, "touch action pull to load not enable");
            return false;
        }
        if (isLoading()) {
            Log.i(TAG, "touch action is loading");
            return true;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            if (isReadyToPull()) {
                Log.i(TAG, "touch action action down");
                mEndX = mStartX = event.getX();
                mEndY = mStartY = event.getY();
                return true;
            }
        }
            break;
        case MotionEvent.ACTION_MOVE: {
            if (mIsIntercepted) {
                Log.i(TAG, "touch action action move");
                mEndX = event.getX();
                mEndY = event.getY();
                handlePull();
                return true;
            }
        }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL: {
            if (mIsIntercepted) {
                mIsIntercepted = false;
                if (mState == State.RELEASE_TO_LOAD) {
                    Log.i(TAG, "touch up | cancel start loading");
                    setState(State.LOADING);
                    return true;
                }

                if (isLoading()) {
                    Log.i(TAG, "touch action up | cancel is loading");
                    return true;
                }
                Log.i(TAG, "touch action up | cancel reset");
                setState(State.RESET);
            }
        }
            break;
        }
        return false;
    }

    /**
     * 是否可以拉动
     * @return true 可以拉动
     */
    private boolean isReadyToPull() {
        switch (mLoadMode) {
        case PULL_FROM_START:
            return isReadyToPullStart();
        case PULL_FROM_END:
            return isReadyToPullEnd();
        case BOTH:
            return isReadyToPullStart() || isReadyToPullEnd();
        default:
            return false;
        }
    }

    /**
     * 垂直方向:向下拉动
     * 水平方向:向右拉动
     * @return true 可以拉动
     */
    private boolean isReadyToPullStart() {
        Log.e(TAG, isReadyToPull(Direction.START) + "  PULL_FROM_START");
        return isReadyToPull(Direction.START);
    }

    /**
     * 垂直方向:向上拉动
     * 水平方向:向左拉动
     * @return true 可以拉动
     */
    private boolean isReadyToPullEnd() {
        Log.e(TAG, isReadyToPull(Direction.END) + "  isReadyToPullEnd");
        return isReadyToPull(Direction.END);
    }

    /**
     * 判断在指定的方向上,view是否还可以继续滑动
     * @param direction 指定的方向
     * @return
     */
    private boolean isReadyToPull(Direction direction) {
        switch (getScrollOrientation()) {
        case VERTICAL:
        default:
            return !canScrollVertical(direction);
        case HORIZONTAL:
            return !canScrollHorizontal(direction);
        }
    }

    /**
     * 设置状态
     * @param state 状态
     */
    private void setState(State state) {
        mState = state;
        switch (mState) {
        case PULL_FROM_START:
            mCurUpdateLayout = mHeader;
            break;
        case PULL_FROM_END:
            mCurUpdateLayout = mFooter;
            break;
        case LOADING:
            onLoading();
            break;
        case RELEASE_TO_LOAD:
            break;
        case MANUAL_LOAD:
            mCurUpdateLayout = mHeader;
            break;
        case OVERSCROLL:
            break;
        case RESET:
            reset();
            break;
        }
        onPull(state, 0);
    }

    private void onLoading() {
        switch (mCurLoadMode) {
        case PULL_FROM_START:
            scroll(-mActionBarSize);
            if (mPullToLoadListener != null) {
                mPullToLoadListener.onLoadNew();
            }
        default:
            break;
        case PULL_FROM_END:
            if (mPullToLoadListener != null) {
                mPullToLoadListener.onLoadMore();
            }
            break;
        }
    }

    /**
     * 重置状态
     */
    private void reset() {
        mCurLoadMode = null;
        mIsIntercepted = false;
        mEndX = mStartX = 0;
        mEndY = mStartY = 0;
        mCurUpdateLayout = null;
        scrollTo(0, 0);
    }

    /**
     * 处理touch事件产生的拖动
     */
    private void handlePull() {
        final float startValue;
        final float endValue;

        switch (getScrollOrientation()) {
        case VERTICAL:
        default: {
            startValue = mStartY;
            endValue = mEndY;
        }
            break;
        case HORIZONTAL: {
            startValue = mStartX;
            endValue = mEndX;
        }
            break;
        }
        Log.i(TAG, "handlePull " + (startValue - endValue));
        final float scrollValue;
        switch (mCurLoadMode) {
        case PULL_FROM_START:
        default:
            scrollValue = Math.round(Math.min(startValue - endValue, 0) / FRICTION);
            break;
        case PULL_FROM_END:
            scrollValue = Math.round(Math.max(startValue - endValue, 0) / FRICTION);
            break;
        }

        if (scrollValue != 0 && !isLoading()) {
            scroll(scrollValue);
            if (Math.abs(scrollValue) > mCurUpdateLayout.getSize()) {
                setState(State.RELEASE_TO_LOAD);
            } else {
                switch (mCurLoadMode) {
                case PULL_FROM_START:
                default: {
                    setState(State.PULL_FROM_START);
                }
                case PULL_FROM_END: {
                    setState(State.PULL_FROM_END);
                }
                }
            }
            onPull(mState, scrollValue);
        }
    }

    private void scroll(float scrollValue) {
        switch (getScrollOrientation()) {
        case VERTICAL:
        default:
            scrollTo(0, (int) scrollValue);
            break;
        case HORIZONTAL:
            scrollTo((int) scrollValue, 0);
            break;
        }
    }
}
