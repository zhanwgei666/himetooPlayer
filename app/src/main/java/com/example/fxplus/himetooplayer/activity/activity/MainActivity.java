package com.example.fxplus.himetooplayer.activity.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import com.example.fxplus.himetooplayer.R;
import com.example.fxplus.himetooplayer.activity.base.BasePager;
import com.example.fxplus.himetooplayer.activity.pager.AudioPager;
import com.example.fxplus.himetooplayer.activity.pager.NetAudioPager;
import com.example.fxplus.himetooplayer.activity.pager.NetVideoPager;
import com.example.fxplus.himetooplayer.activity.pager.VideoPager;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private FrameLayout fl_main_content;

    private RadioGroup rg_bottom_tag;

    //页面集合
    private ArrayList<BasePager> basePagers;

    //选中的位置
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fl_main_content = (FrameLayout) findViewById(R.id.fl_main_content);
        rg_bottom_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);

        basePagers = new ArrayList<>();
        basePagers.add(new VideoPager(this));//添加本地视频页面  -0
        basePagers.add(new AudioPager(this));//添加本地音乐页面  -1
        basePagers.add(new NetVideoPager(this));//添加网络视频页面  -2
        basePagers.add(new NetAudioPager(this));//添加网络音乐页面   -3

        //设置RadioGroup的监听
        rg_bottom_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        rg_bottom_tag.check(R.id.rb_video);//默认选中首页
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            switch (checkedId) {
                default:
                    position = 0;
                    break;
                case R.id.rb_audio://音频
                    position = 1;
                    break;
                case R.id.rb_netvideo:
                    position = 2;
                    break;
                case R.id.rb_netaudio:
                    position = 3;
                    break;
            }
            setFragment();
        }
    }
    //把页面添加到Fragment中
    private void setFragment() {
        //1、得到FragmentMangaer
        FragmentManager manager = getFragmentManager();
        //2、开启事务
        FragmentTransaction ft = manager.beginTransaction();
        //3、替换
        ft.replace(R.id.fl_main_content,new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                BasePager basePager = getBasePager();
                if (basePager != null){
                    return basePager.rootview;
                }
                return null;
            }
        });
        //4、提交事务
        ft.commit();
    }

    //根据位置得到对应的页面
    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData){
            basePager.initData();//联网请求或者绑定数据
            basePager.isInitData = true;
        }
        return basePager;
    }
}
