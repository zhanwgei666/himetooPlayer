package com.example.fxplus.himetooplayer.activity.pager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fxplus.himetooplayer.R;

import com.example.fxplus.himetooplayer.activity.activity.AudioPlayerAcitvity;
import com.example.fxplus.himetooplayer.activity.adapter.VideoPagerAdapetrAdapter;
import com.example.fxplus.himetooplayer.activity.base.BasePager;
import com.example.fxplus.himetooplayer.activity.domain.MediaItem;

import java.util.ArrayList;

//本地音乐页面

public class AudioPager extends BasePager {

    private ListView listView;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;

    private VideoPagerAdapetrAdapter VideoPagerAdapetrAdapter;

    //数据集合
    private ArrayList<MediaItem> mediaItems;

    public AudioPager(Context context) {
        super(context);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems != null && mediaItems.size() > 0) {
                //有数据
                //设置适配器
                VideoPagerAdapetrAdapter = new VideoPagerAdapetrAdapter(context,mediaItems,false);
                listView.setAdapter(VideoPagerAdapetrAdapter);
                //把文本隐藏
                tv_nomedia.setVisibility(View.GONE);
            } else {
                //没有数据
                //文本显示
                tv_nomedia.setVisibility(View.VISIBLE);
                tv_nomedia.setText("没有发现音频....");
            }
            //ProgressBar隐藏
            pb_loading.setVisibility(View.GONE);
        }
    };

    //初始化当前页面的控件，由父类调用
    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.video_pager, null);
        listView = (ListView) view.findViewById(R.id.listView);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        //设置ListView的Item的点击事件
        listView.setOnItemClickListener(new MyOnItemClickListener());
        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //3.传递列表数据-对象-序列化
            Intent intent = new Intent(context, AudioPlayerAcitvity.class);
            intent.putExtra("position",position);
            context.startActivity(intent);
        }
    }

    @Override
    public void initData() {
        super.initData();
        //加载本地视频数据
        getDataFromLocal();
    }

    //从本地SD卡得到数据
    //遍历SD卡，后缀名
    //从内容提供者里面获取视频
    private void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mediaItems = new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();//获取内容提供者
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频文件在SD卡的名称
                        MediaStore.Audio.Media.DURATION,//视频总时长
                        MediaStore.Audio.Media.SIZE,//视频文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST,//艺术家
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);//添加数据

                        String name = cursor.getString(0);//视频的名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);//视频的时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);//视频的文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);//视频的播放地址
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);//艺术家
                        mediaItem.setArtist(artist);
                    }
                    cursor.close();
                }

                //Handler发消息
                handler.sendEmptyMessage(10);
            }
        }.start();
    }

    //解决安卓6.0以上不能读取外部存储数据的权限问题
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
        return true;
    }

}
