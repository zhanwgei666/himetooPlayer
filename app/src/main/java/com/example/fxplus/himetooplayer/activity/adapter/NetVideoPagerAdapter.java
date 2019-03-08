package com.example.fxplus.himetooplayer.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fxplus.himetooplayer.R;
import com.example.fxplus.himetooplayer.activity.domain.MediaItem;
import com.example.fxplus.himetooplayer.activity.domain.NetAudioPagerData;
import com.example.fxplus.himetooplayer.activity.pager.NetAudioPager;

import org.xutils.common.util.DensityUtil;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import pl.droidsonroids.gif.GifImageView;

/**
 * 作用：NetVideoPagerAdapter的适配器(分类型ListView)
 */
public class NetVideoPagerAdapter extends BaseAdapter {





    private  Context context;
    private ArrayList<MediaItem> mediaItems;

    public NetVideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems){
        this.context = context;
        this.mediaItems = mediaItems;
    }

    @Override
    public int getCount() {
        return mediaItems.size();
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
        if(convertView ==null){
            convertView = View.inflate(context, R.layout.item_netvideo_pager,null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);

            convertView.setTag(viewHoder);
        }else{
            viewHoder = (ViewHoder) convertView.getTag();
        }

        //根据position得到列表中对应位置的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_desc.setText(mediaItem.getDesc());
        //1.使用xUtils3请求图片
        x.image().bind(viewHoder.iv_icon,mediaItem.getImageUrl());
        //2.使用Glide请求图片
//        Glide.with(context).load(mediaItem.getImageUrl())
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.video_default)
//                .error(R.drawable.video_default)
//                .into(viewHoder.iv_icon);

        //3.使用Picasso 请求图片
//        Picasso.with(context).load(mediaItem.getImageUrl())
////                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.video_default)
//                .error(R.drawable.video_default)
//                .into(viewHoder.iv_icon);


        return convertView;
    }


    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }

}

