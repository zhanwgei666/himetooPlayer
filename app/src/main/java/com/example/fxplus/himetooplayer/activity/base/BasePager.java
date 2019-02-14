package com.example.fxplus.himetooplayer.activity.base;

import android.content.Context;
import android.view.View;
//作用：公共类，基类

public abstract class BasePager {
    //上下文
    public final Context context;

    public View rootview;
    public boolean isInitData;

    public BasePager(Context context){
        this.context = context;
        rootview = initView();
    }

    //强制由孩子实现，实现特定的效果
    public abstract View initView();

    //当子页面需要初始化数据，联网请求数据，或者绑定数据的时候要重写该方法
    public void initData(){

    }
}
