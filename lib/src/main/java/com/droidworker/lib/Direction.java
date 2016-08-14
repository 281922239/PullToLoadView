package com.droidworker.lib;

/**
 * 代表方向的枚举
 * @author https://github.com/DroidWorkerLYF
 */
public enum Direction {
    /**
     * 还能不能向视图底部滑动,例如,一个垂直的滚动列表还能不能向下滚动
     */
    END(-1),
    /**
     * 还能不能向视图顶部滑动,例如,一个水平滚动的列表还能不能向左滚动
     */
    START(1);

    private int mIntValue;

    Direction(int intValue){
        mIntValue = intValue;
    }

    int getIntValue(){
        return mIntValue;
    }
}
