package com.example.fxplus.himetooplayer.activity.pager;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.example.fxplus.himetooplayer.activity.base.BasePager;
import com.example.fxplus.himetooplayer.activity.util.LogUtil;

//本地视频页面

public class VideoPager extends BasePager {

    private TextView textView;
    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        LogUtil.e("本地视频页面被初始化");
        textView = new TextView(context);
        textView.setTextSize(25);
        textView.setTextColor(Color.RED);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("本地视频页面数据被初始化了");
        textView.setText("本地视频页面");
    }
}
