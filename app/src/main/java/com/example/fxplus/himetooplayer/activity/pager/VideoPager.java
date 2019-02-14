package com.example.fxplus.himetooplayer.activity.pager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;

import com.example.fxplus.himetooplayer.R;
import com.example.fxplus.himetooplayer.activity.base.BasePager;
import com.example.fxplus.himetooplayer.activity.domain.MediaItem;
import com.example.fxplus.himetooplayer.activity.util.LogUtil;
import com.example.fxplus.himetooplayer.activity.util.Utils;

import java.util.ArrayList;

//本地视频页面

public class VideoPager extends BasePager {
    private ListView listView;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private Utils utils;
    private VideoPagerAdagerAdapter videoPagerAdagerAdapter;

    //数据集合
    private ArrayList<MediaItem> mediaItems;

    public VideoPager(Context context) {
        super(context);
        utils = new Utils();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaItems != null && mediaItems.size() > 0) {
                //有数据
                //设置适配器
                videoPagerAdagerAdapter = new VideoPagerAdagerAdapter();
                listView.setAdapter(videoPagerAdagerAdapter);
                //把文本隐藏
                tv_nomedia.setVisibility(View.GONE);
            } else {
                //没有数据
                //文本显示
                tv_nomedia.setVisibility(View.VISIBLE);
            }
            //ProgressBar隐藏
            pb_loading.setVisibility(View.GONE);
        }
    };

    //初始化当前页面的控件，由父类调用
    @Override
    public View initView() {
        LogUtil.e("本地视频页面被初始化");
        View view = View.inflate(context, R.layout.video_pager, null);
        listView = (ListView) view.findViewById(R.id.listView);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);

        return view;
    }

    @Override
    public void initData() {
        super.initData();
        //加载本地视频数据
        getDataFromLocal();
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

    //从本地SD卡得到数据
    //遍历SD卡，后缀名
    //从内容提供者里面获取视频
    private void getDataFromLocal() {
        mediaItems = new ArrayList<>();
        new Thread() {
            @Override
            public void run() {
                super.run();

                ContentResolver resolver = context.getContentResolver();//获取内容提供者

                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件在SD卡的名称
                        MediaStore.Video.Media.DURATION,//视频总时长
                        MediaStore.Video.Media.SIZE,//视频文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST,//艺术家
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

    class VideoPagerAdagerAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoder viewHoder;
            if (convertView != null){
                convertView = View.inflate(context,R.layout.item_video_pager,null);
                viewHoder = new ViewHoder();
                viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                viewHoder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
                viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHoder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(viewHoder);
            }else {
                viewHoder = (ViewHoder) convertView.getTag();
            }

            //根据position得到列表中对应位置的数据
            MediaItem mediaItem = mediaItems.get(position);
            viewHoder.tv_name.setText(mediaItem.getName());
            viewHoder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
            viewHoder.tv_time.setText(utils.stringForTime((int) mediaItem.getDuration()));


            return convertView;
        }
    }
    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_size;
        TextView tv_time;
    }

}
