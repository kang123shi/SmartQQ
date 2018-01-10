package com.fanbo.taokehelper.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Administrator on 2018/1/7.
 */

public class QQLIstView extends ListView {
    public QQLIstView(Context context) {
        super(context);
    }

    public QQLIstView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QQLIstView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
