package com.droidworker.pulltoloadview.constant;

/**
 * 加载模式
 * @author https://github.com/DroidWorkerLYF
 */
public enum LoadMode {
    /**
     * 从头部拉动,加载更新
     */
    PULL_FROM_START,
    /**
     * 从头部拉动加载更新,自动加载更多
     */
    PULL_FROM_START_AUTO_LOAD_MORE,
    /**
     * 从头部拉动加载更新,自动加载更多
     */
    PULL_FROM_START_AUTO_LOAD_MORE_WITH_FOOTER,
    /**
     * 从尾部拉动,加载更多
     */
    PULL_FROM_END,
    /**
     * 同时支持PULL_FROM_START和PULL_FROM_END
     */
    BOTH,
    /**
     * 禁止拉动
     */
    DISABLED,
    /**
     * 不支持手动
     */
    MANUAL_ONLY;

    public boolean isPullToLoad() {
        return !(this == DISABLED || this == MANUAL_ONLY);
    }

    public boolean isPullFromStart() {
        return this == PULL_FROM_START || this == BOTH || this == PULL_FROM_START_AUTO_LOAD_MORE;
    }

    public boolean isPullFromEnd() {
        return this == PULL_FROM_END || this == BOTH || this == PULL_FROM_START_AUTO_LOAD_MORE;
    }

    public boolean shouldShowHeader() {
        return this == PULL_FROM_START || this == MANUAL_ONLY || this == PULL_FROM_START_AUTO_LOAD_MORE || this == BOTH;
    }

    public boolean shouldShowFooter() {
        return this == PULL_FROM_END || this == BOTH;
    }

    public boolean canOverScrollStart() {
        return this == MANUAL_ONLY || this == PULL_FROM_END || this == DISABLED;
    }

    public boolean canOverScrollEnd() {
        return this == PULL_FROM_START || this == MANUAL_ONLY || this == PULL_FROM_START_AUTO_LOAD_MORE || this == DISABLED;
    }

    public boolean isAutoLoadMore(){
        return this == PULL_FROM_START_AUTO_LOAD_MORE || this == PULL_FROM_START_AUTO_LOAD_MORE_WITH_FOOTER;
    }

    public boolean shouldShowAutoLoadMoreFooter(){
        return this == PULL_FROM_START_AUTO_LOAD_MORE_WITH_FOOTER;
    }
}
